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
        if (consultationId == null || medecinSpecialisteId == null || creneauId == null
                || question == null || question.isBlank()) {
            throw new IllegalArgumentException("La consultation, le spécialiste, le créneau et la question sont obligatoires.");
        }
        Consultation consultation = consultationDao.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("Consultation introuvable : " + consultationId));
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
        if (consultationId != null) {
            return demandeDao.findByConsultation_IdOrderByIdDesc(consultationId);
        }
        return statut == null || statut.isBlank()
                ? demandeDao.findAll()
                : demandeDao.findByStatut(statut);
    }

    @Transactional(readOnly = true)
    public DemandeExpertise charger(Long id) {
        return demandeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'expertise introuvable : " + id));
    }

    @Transactional
    public DemandeExpertise repondre(Long id, String avisSpecialiste) {
        if (avisSpecialiste == null || avisSpecialiste.isBlank()) {
            throw new IllegalArgumentException("L'avis du spécialiste est obligatoire.");
        }
        DemandeExpertise demande = charger(id);
        if ("REPONDUE".equals(demande.getStatut()) || "ANNULEE".equals(demande.getStatut())) {
            throw new IllegalStateException("Cette demande ne peut plus recevoir de réponse.");
        }
        demande.setAvisSpecialiste(avisSpecialiste.trim());
        demande.setDateReponse(LocalDateTime.now());
        demande.setStatut("REPONDUE");
        return demandeDao.save(demande);
    }

    @Transactional
    public DemandeExpertise annuler(Long id) {
        DemandeExpertise demande = charger(id);
        if ("REPONDUE".equals(demande.getStatut()) || "ANNULEE".equals(demande.getStatut())) {
            throw new IllegalStateException("Cette demande ne peut plus être annulée.");
        }
        Creneau creneau = demande.getCreneau();
        if (creneau != null && !creneau.isDisponible()
                && creneau.getDateHeure() != null && creneau.getDateHeure().isAfter(LocalDateTime.now())) {
            creneauService.annulerReservation(creneau.getId());
        }
        demande.setStatut("ANNULEE");
        return demandeDao.save(demande);
    }
}
