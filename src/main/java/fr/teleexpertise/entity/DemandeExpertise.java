package fr.teleexpertise.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class DemandeExpertise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;
    private String priorite = "NORMALE";
    private String statut = "EN_ATTENTE_AVIS_SPECIALISTE";
    private String modeEchange;
    private String avisSpecialiste;
    private LocalDateTime dateReponse;

    @ManyToOne
    private Consultation consultation;

    @ManyToOne
    private Medecin medecinSpecialiste;

    @ManyToOne
    private Creneau creneau;

    public DemandeExpertise() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getModeEchange() {
        return modeEchange;
    }

    public void setModeEchange(String modeEchange) {
        this.modeEchange = modeEchange;
    }

    public String getAvisSpecialiste() {
        return avisSpecialiste;
    }

    public void setAvisSpecialiste(String avisSpecialiste) {
        this.avisSpecialiste = avisSpecialiste;
    }

    public LocalDateTime getDateReponse() {
        return dateReponse;
    }

    public void setDateReponse(LocalDateTime dateReponse) {
        this.dateReponse = dateReponse;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public void setConsultation(Consultation consultation) {
        this.consultation = consultation;
    }

    public Medecin getMedecinSpecialiste() {
        return medecinSpecialiste;
    }

    public void setMedecinSpecialiste(Medecin medecinSpecialiste) {
        this.medecinSpecialiste = medecinSpecialiste;
    }

    public Creneau getCreneau() {
        return creneau;
    }

    public void setCreneau(Creneau creneau) {
        this.creneau = creneau;
    }
}