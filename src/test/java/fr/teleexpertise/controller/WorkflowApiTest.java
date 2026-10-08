package fr.teleexpertise.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkflowApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticated_user_can_complete_clinical_workflow() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());

        MvcResult bootstrap = mockMvc.perform(post("/api/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Admin","prenom":"Test","username":"admin-test","password":"secure-test-password"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) bootstrap.getRequest().getSession(false);

        String csrf = mockMvc.perform(get("/api/auth/status").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String csrfToken = objectMapper.readTree(csrf).get("csrfToken").asText();

        String patientBody = """
                {"nom":"Martin","prenom":"Lea","telephone":"0600000000","adresse":"Paris"}
                """;
        String patientJson = mockMvc.perform(post("/api/patients").session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patientBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Martin"))
                .andReturn().getResponse().getContentAsString();
        long patientId = objectMapper.readTree(patientJson).get("id").asLong();

        String medecinJson = mockMvc.perform(post("/api/medecins").session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Diallo","prenom":"Amine","specialite":"Cardiologie","tarif":50,"disponible":true}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long medecinId = objectMapper.readTree(medecinJson).get("id").asLong();

        String consultationJson = mockMvc.perform(post("/api/consultations").session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"patientId":%d,"medecinGeneralisteId":%d,"symptomes":"Douleur thoracique","observationsCliniques":"Stable"}
                                """.formatted(patientId, medecinId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long consultationId = objectMapper.readTree(consultationJson).get("id").asLong();

        String slotJson = mockMvc.perform(post("/api/creneaux").session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"medecinId":%d,"dateHeure":"2099-06-01T10:00:00"}
                                """.formatted(medecinId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long slotId = objectMapper.readTree(slotJson).get("id").asLong();

        String demandeJson = mockMvc.perform(post("/api/demandes-expertise").session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"consultationId":%d,"medecinSpecialisteId":%d,"creneauId":%d,"question":"Avis cardiologique","priorite":"URGENTE","modeEchange":"SYNCHRONE"}
                                """.formatted(consultationId, medecinId, slotId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_AVIS_SPECIALISTE"))
                .andExpect(jsonPath("$.creneau.disponible").value(false))
                .andReturn().getResponse().getContentAsString();
        long demandeId = objectMapper.readTree(demandeJson).get("id").asLong();

        mockMvc.perform(get("/api/consultations/" + consultationId).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_AVIS_SPECIALISTE"));

        mockMvc.perform(post("/api/demandes-expertise/" + demandeId + "/reponse").session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"avisSpecialiste":"Avis transmis"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REPONDUE"));

        mockMvc.perform(post("/api/admissions").session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"patientId":%d,"infirmier":"Samira","tensionArterielle":"120/80","frequenceCardiaque":72,"temperature":37.1,"frequenceRespiratoire":16,"poids":65,"taille":168}
                                """.formatted(patientId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_MEDECIN_GENERALISTE"));

        mockMvc.perform(put("/api/consultations/" + consultationId).session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"symptomes":"Douleur thoracique","observationsCliniques":"Stable","diagnostic":"À préciser","traitement":"Surveillance","statut":"TERMINEE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TERMINEE"));
    }

    @Test
    void authenticated_write_requires_csrf_token() throws Exception {
        MvcResult bootstrap = mockMvc.perform(post("/api/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Admin","prenom":"Test","username":"admin-csrf","password":"secure-test-password"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) bootstrap.getRequest().getSession(false);

        mockMvc.perform(post("/api/patients").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Martin","prenom":"Lea"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Jeton de sécurité absent ou invalide."));
    }
}
