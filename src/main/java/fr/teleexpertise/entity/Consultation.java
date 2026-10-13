package fr.teleexpertise.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateHeure;
    private String symptomes;
    private String observationsCliniques;
    private String diagnostic;
    private String traitement;
    private String statut = "EN_COURS";
    private BigDecimal tarifDh = new BigDecimal("150.00");

    @ElementCollection
    @CollectionTable(name = "consultation_actes_techniques",
            joinColumns = @JoinColumn(name = "consultation_id"))
    private List<ActeTechnique> actesTechniques = new ArrayList<>();

    @ManyToOne
    private Patient patient;

    @ManyToOne
    private Medecin medecinGeneraliste;

    public Consultation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getSymptomes() {
        return symptomes;
    }

    public void setSymptomes(String symptomes) {
        this.symptomes = symptomes;
    }

    public String getObservationsCliniques() {
        return observationsCliniques;
    }

    public void setObservationsCliniques(String observationsCliniques) {
        this.observationsCliniques = observationsCliniques;
    }

    public String getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(String diagnostic) {
        this.diagnostic = diagnostic;
    }

    public String getTraitement() {
        return traitement;
    }

    public void setTraitement(String traitement) {
        this.traitement = traitement;
    }

    public BigDecimal getTarifDh() {
        return tarifDh;
    }

    public void setTarifDh(BigDecimal tarifDh) {
        this.tarifDh = tarifDh;
    }

    public List<ActeTechnique> getActesTechniques() {
        return actesTechniques;
    }

    public void setActesTechniques(List<ActeTechnique> actesTechniques) {
        this.actesTechniques = actesTechniques == null ? new ArrayList<>() : new ArrayList<>(actesTechniques);
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Medecin getMedecinGeneraliste() {
        return medecinGeneraliste;
    }

    public void setMedecinGeneraliste(Medecin medecinGeneraliste) {
        this.medecinGeneraliste = medecinGeneraliste;
    }
}