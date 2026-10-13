package fr.teleexpertise.entity;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;

@Embeddable
public class ActeTechnique {

    private String type;
    private BigDecimal tarifDh;

    public ActeTechnique() {
    }

    public ActeTechnique(String type, BigDecimal tarifDh) {
        this.type = type;
        this.tarifDh = tarifDh;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getTarifDh() {
        return tarifDh;
    }

    public void setTarifDh(BigDecimal tarifDh) {
        this.tarifDh = tarifDh;
    }
}
