    package fr.teleexpertise.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.teleexpertise.dao.ConsultationDao;
import fr.teleexpertise.dao.DemandeExpertiseDao;
import fr.teleexpertise.dao.MedecinDao;
import fr.teleexpertise.entity.Consultation;
import fr.teleexpertise.entity.Creneau;
import fr.teleexpertise.entity.DemandeExpertise;
import fr.teleexpertise.entity.Medecin;

@Service
public class DemandeExpertiseService {

    private final DemandeExpertiseDao demandeDao;
    private final ConsultationDao consultationDao;
    private final MedecinDao medecinDao;
    private final CreneauService creneauService;

    public DemandeExpertiseService(DemandeExpertiseDao demandeDao, ConsultationDao consultationDao,
            MedecinDao medecinDao, CreneauService creneauService) {
        this.demandeDao = demandeDao;
        this.consultationDao = consultationDao;
        this.medecinDao = medecinDao;
        this.creneauService = creneauService;
    }

    @Transactional
    public DemandeExpertise creer(Long consultationId, Long medecinSpecialisteId, Long creneauId,
            String question, String priorite, String modeEchange) {
        return creer(consultationId, medecinSpecialisteId, creneauId, question, priorite, modeEchange, null);
    }

    @Transactional
    public DemandeExpertise creer(Long consultationId, Long medecinSpecialisteId, Long creneauId,
            String question, String priorite, String modeEchange, Long medecinGeneralisteId) {
        if (consultationId == null || medecinSpecialisteId == null || creneauId == null
                || question == null || question.isBlank()) {
            throw new IllegalArgumentException("La consultation, le spécialiste, le créneau et la question sont obligatoires.");
        }
        Consultation consultation = consultationDao.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation introuvable : " + consultationId));
        if (medecinGeneralisteId != null && (consultation.getMedecinGeneraliste() == null
                || !medecinGeneralisteId.equals(consultation.getMedecinGeneraliste().getId()))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Cette consultation n'appartient pas à votre profil médecin.");
        }
        Medecin medecin = medecinDao.findById(medecinSpecialisteId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable : " + medecinSpecialisteId));
        if (priorite == null || priorite.isBlank()) {
            priorite = "NORMALE";
        }
        if (!priorite.equals("URGENTE") && !priorite.equals("NORMALE")
                && !priorite.equals("NON URGENTE")) {
            throw new IllegalArgumentException("La priorité doit être URGENTE, NORMALE ou NON URGENTE.");
        }
        if (modeEchange == null || modeEchange.isBlank()) {
            modeEchange = "ASYNCHRONE";
        }
        if (!modeEchange.equals("SYNCHRONE") && !modeEchange.equals("ASYNCHRONE")) {
            throw new IllegalArgumentException("Le mode doit être SYNCHRONE ou ASYNCHRONE.");
        }

        DemandeExpertise demande = new DemandeExpertise();
        demande.setQuestion(question.trim());
        demande.setPriorite(priorite);
        demande.setModeEchange(modeEchange);
        demande.setConsultation(consultation);
        demande.setMedecinSpecialiste(medecin);
        demande.setCreneau(creneauService.reserver(creneauId, medecinSpecialisteId));
        demande.setStatut("EN_ATTENTE_AVIS_SPECIALISTE");
        consultation.setStatut("EN_ATTENTE_AVIS_SPECIALISTE");
        consultationDao.save(consultation);
        return demandeDao.save(demande);
    }

    @Transactional(readOnly = true)
    public List<DemandeExpertise> lister(String statut, Long consultationId) {
        return lister(statut, null, consultationId);
    }

    @Transactional(readOnly = true)
    public List<DemandeExpertise> lister(String statut, String priorite, Long consultationId) {
        List<DemandeExpertise> demandes = consultationId != null
                ? demandeDao.findByConsultation_IdOrderByIdDesc(consultationId)
                : statut == null || statut.isBlank()
                        ? demandeDao.findAll()
                        : demandeDao.findByStatut(statut);
        return demandes.stream()
                .filter(demande -> priorite == null || priorite.isBlank()
                        || priorite.equalsIgnoreCase(demande.getPriorite()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DemandeExpertise> lister(String statut, String priorite, Long consultationId,
            String role, Long medecinId) {
        if ("ADMIN".equals(role)) {
            return lister(statut, priorite, consultationId);
        }
        List<DemandeExpertise> demandes = "MEDECIN_SPECIALISTE".equals(role)
                ? demandeDao.findByMedecinSpecialiste_Id(medecinId)
                : demandeDao.findByConsultation_MedecinGeneraliste_Id(medecinId);
        return demandes.stream()
                .filter(demande -> consultationId == null
                        || consultationId.equals(demande.getConsultation().getId()))
                .filter(demande -> statut == null || statut.isBlank() || statut.equals(demande.getStatut()))
                .filter(demande -> priorite == null || priorite.isBlank()
                        || priorite.equalsIgnoreCase(demande.getPriorite()))
                .toList();
    }

    @Transactional(readOnly = true)
    public DemandeExpertise charger(Long id) {
        return demandeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'expertise introuvable : " + id));
    }

    @Transactional
    public DemandeExpertise repondre(Long id, String avisSpecialiste) {
        return repondre(id, avisSpecialiste, null);
    }

    @Transactional
    public DemandeExpertise repondre(Long id, String avisSpecialiste, String recommandations) {
        return repondre(id, avisSpecialiste, recommandations, null);
    }

    @Transactional
    public DemandeExpertise repondre(Long id, String avisSpecialiste, String recommandations,
            Long medecinSpecialisteId) {
        if (avisSpecialiste == null || avisSpecialiste.isBlank()) {
            throw new IllegalArgumentException("L'avis du spécialiste est obligatoire.");
        }
        DemandeExpertise demande = charger(id);
        verifierSpecialisteProprietaire(demande, medecinSpecialisteId);
        if ("REPONDUE".equals(demande.getStatut()) || "ANNULEE".equals(demande.getStatut())) {
            throw new IllegalStateException("Cette demande ne peut plus recevoir de réponse.");
        }
        demande.setAvisSpecialiste(avisSpecialiste.trim());
        demande.setRecommandations(recommandations == null || recommandations.isBlank()
                ? null : recommandations.trim());
        demande.setDateReponse(LocalDateTime.now());
        demande.setStatut("REPONDUE");
        Consultation consultation = demande.getConsultation();
        if (consultation != null) {
            consultation.setStatut("TERMINEE");
            consultationDao.save(consultation);
        }
        return demandeDao.save(demande);
    }

    @Transactional
    public DemandeExpertise annuler(Long id) {
        return annuler(id, null);
    }

    @Transactional
    public DemandeExpertise annuler(Long id, Long medecinGeneralisteId) {
        DemandeExpertise demande = charger(id);
        if (medecinGeneralisteId != null && (demande.getConsultation() == null
                || demande.getConsultation().getMedecinGeneraliste() == null
                || !medecinGeneralisteId.equals(demande.getConsultation().getMedecinGeneraliste().getId()))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Cette demande ne relève pas de votre profil médecin.");
        }
        if ("REPONDUE".equals(demande.getStatut()) || "ANNULEE".equals(demande.getStatut())) {
            throw new IllegalStateException("Cette demande ne peut plus être annulée.");
        }
        Creneau creneau = demande.getCreneau();
        if (creneau != null && !creneau.isDisponible()
                && creneau.getDateHeure() != null && creneau.getDateHeure().isAfter(LocalDateTime.now())) {
            creneauService.annulerReservation(creneau.getId());
        }
        demande.setStatut("ANNULEE");
        Consultation consultation = demande.getConsultation();
        if (consultation != null && demandeDao.findByConsultation_IdOrderByIdDesc(consultation.getId()).stream()
                .noneMatch(autre -> !autre.getId().equals(demande.getId())
                        && "EN_ATTENTE_AVIS_SPECIALISTE".equals(autre.getStatut()))) {
            consultation.setStatut("EN_COURS");
            consultationDao.save(consultation);
        }
        return demandeDao.save(demande);
    }

    private void verifierSpecialisteProprietaire(DemandeExpertise demande, Long medecinSpecialisteId) {
        if (medecinSpecialisteId != null && (demande.getMedecinSpecialiste() == null
                || !medecinSpecialisteId.equals(demande.getMedecinSpecialiste().getId()))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Cette demande n'est pas adressée à votre profil médecin.");
        }
    }
}
