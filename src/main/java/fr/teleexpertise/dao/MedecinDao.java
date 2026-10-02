    package fr.teleexpertise.dao;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.teleexpertise.entity.Medecin;

public interface MedecinDao extends JpaRepository<Medecin, Long> {

    default List<Medecin> rechercherParSpecialiteTrieeParTarif(String specialite) {
        return findAll().stream()
                .filter(medecin -> medecin.getSpecialite() != null
                        && medecin.getSpecialite().equalsIgnoreCase(specialite))
                .sorted(Comparator.comparingDouble(Medecin::getTarif))
                .collect(Collectors.toList());
    }
}