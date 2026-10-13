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
                {"nom":"Martin","prenom":"Lea","dateNaissance":"1985-04-03","numeroSecuriteSociale":"TEST-123456","telephone":"0600000000","adresse":"Paris"}
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

        mockMvc.perform(put("/api/consultations/" + consultationId).session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"symptomes":"Douleur thoracique","statut":"TERMINEE"}
                                """))
                .andExpect(status().isConflict());

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

    @Test
    void patient_search_scheduling_costs_and_specialist_recommendations_work() throws Exception {
        MockHttpSession adminSession = bootstrapAdmin("admin-requirements");
        String adminCsrf = csrfToken(adminSession);

        String patientJson = mockMvc.perform(post("/api/patients").session(adminSession)
                        .header("X-CSRF-TOKEN", adminCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Martin","prenom":"Lea","dateNaissance":"1985-04-03","numeroSecuriteSociale":"TEST-19850403"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dateNaissance").value("1985-04-03"))
                .andReturn().getResponse().getContentAsString();
        long patientId = objectMapper.readTree(patientJson).get("id").asLong();

        mockMvc.perform(get("/api/patients?numeroSecuriteSociale=TEST-1985").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(patientId));
        mockMvc.perform(post("/api/patients").session(adminSession)
                        .header("X-CSRF-TOKEN", adminCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Duplicate","prenom":"Patient","dateNaissance":"1985-04-03","numeroSecuriteSociale":"TEST-19850403"}
                                """))
                .andExpect(status().isBadRequest());

        String doctorJson = mockMvc.perform(post("/api/medecins").session(adminSession)
                        .header("X-CSRF-TOKEN", adminCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Diallo","prenom":"Amine","specialite":"Cardiologie","tarif":50,"disponible":true}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long doctorId = objectMapper.readTree(doctorJson).get("id").asLong();

        String consultationJson = mockMvc.perform(post("/api/consultations").session(adminSession)
                        .header("X-CSRF-TOKEN", adminCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"patientId":%d,"medecinGeneralisteId":%d,"symptomes":"Douleur thoracique"}
                                """.formatted(patientId, doctorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tarifDh").value(150.00))
                .andReturn().getResponse().getContentAsString();
        long consultationId = objectMapper.readTree(consultationJson).get("id").asLong();

        mockMvc.perform(post("/api/consultations/" + consultationId + "/actes-techniques")
                        .session(adminSession)
                        .header("X-CSRF-TOKEN", adminCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"Radiographie","tarifDh":25.50}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/consultations/" + consultationId + "/cout").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultationDh").value(150.00))
                .andExpect(jsonPath("$.actesTechniquesDh").value(25.50))
                .andExpect(jsonPath("$.totalDh").value(175.50));

        String planningJson = mockMvc.perform(post("/api/creneaux/planning").session(adminSession)
                        .header("X-CSRF-TOKEN", adminCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"medecinId":%d,"date":"2099-06-01"}
                                """.formatted(doctorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].dureeMinutes").value(30))
                .andExpect(jsonPath("$[3].disponible").value(false))
                .andReturn().getResponse().getContentAsString();
        long slotId = objectMapper.readTree(planningJson).get(0).get("id").asLong();
        mockMvc.perform(post("/api/creneaux/planning").session(adminSession)
                        .header("X-CSRF-TOKEN", adminCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"medecinId":%d,"date":"2099-06-01"}
                                """.formatted(doctorId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/auth/users").session(adminSession)
                        .header("X-CSRF-TOKEN", adminCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Diallo","prenom":"Amine","username":"specialist-requirements","password":"secure-test-password","role":"MEDECIN_SPECIALISTE","medecinId":%d}
                                """.formatted(doctorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medecinId").value(doctorId));

        MvcResult specialistLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"specialist-requirements","password":"secure-test-password"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession specialistSession = (MockHttpSession) specialistLogin.getRequest().getSession(false);
        String specialistCsrf = csrfToken(specialistSession);

        String demandJson = mockMvc.perform(post("/api/demandes-expertise").session(adminSession)
                        .header("X-CSRF-TOKEN", adminCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"consultationId":%d,"medecinSpecialisteId":%d,"creneauId":%d,"question":"Avis cardiologique","priorite":"URGENTE"}
                                """.formatted(consultationId, doctorId, slotId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long demandId = objectMapper.readTree(demandJson).get("id").asLong();

        mockMvc.perform(get("/api/demandes-expertise?priorite=URGENTE").session(specialistSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(post("/api/demandes-expertise/" + demandId + "/reponse")
                        .session(specialistSession)
                        .header("X-CSRF-TOKEN", specialistCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"avisSpecialiste":"Avis rassurant","recommandations":"Contrôle dans un mois"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommandations").value("Contrôle dans un mois"));

        mockMvc.perform(get("/api/consultations/" + consultationId).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TERMINEE"));
        mockMvc.perform(get("/api/consultations/" + consultationId + "/cout").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expertisesDh").value(50.00))
                .andExpect(jsonPath("$.totalDh").value(225.50));
    }

    @Test
    void nurse_role_cannot_create_consultations() throws Exception {
        MockHttpSession adminSession = bootstrapAdmin("admin-role-check");
        String adminCsrf = csrfToken(adminSession);
        mockMvc.perform(post("/api/auth/users").session(adminSession)
                        .header("X-CSRF-TOKEN", adminCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Infirmiere","prenom":"Test","username":"nurse-role-check","password":"secure-test-password","role":"INFIRMIER"}
                                """))
                .andExpect(status().isOk());

        MvcResult nurseLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"nurse-role-check","password":"secure-test-password"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession nurseSession = (MockHttpSession) nurseLogin.getRequest().getSession(false);
        String nurseCsrf = csrfToken(nurseSession);

        mockMvc.perform(post("/api/admissions").session(nurseSession)
                        .header("X-CSRF-TOKEN", nurseCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Patient","prenom":"Nouveau","dateNaissance":"1990-05-10","numeroSecuriteSociale":"NURSE-INTAKE-1","infirmier":"Test","tensionArterielle":"120/80","frequenceCardiaque":70,"temperature":36.8,"frequenceRespiratoire":15}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patient.nom").value("Patient"))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_MEDECIN_GENERALISTE"));
        mockMvc.perform(get("/api/admissions").session(nurseSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(post("/api/consultations").session(nurseSession)
                        .header("X-CSRF-TOKEN", nurseCsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    private MockHttpSession bootstrapAdmin(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/bootstrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Admin","prenom":"Test","username":"%s","password":"secure-test-password"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private String csrfToken(MockHttpSession session) throws Exception {
        String status = mockMvc.perform(get("/api/auth/status").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(status).get("csrfToken").asText();
    }
}
