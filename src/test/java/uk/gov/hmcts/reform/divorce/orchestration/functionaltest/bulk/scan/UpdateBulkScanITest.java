package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.controller.BulkScanController.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan.S2SAuthTokens.ALLOWED_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.functionaltest.bulk.scan.S2SAuthTokens.I_AM_NOT_ALLOWED_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class UpdateBulkScanITest {

    private static final String UPDATE_URL = "/update-case";
    private static final String AOS_PACK_OFFLINE_FORM_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/aosPackOfflineForm.json";
    private static final String INVALID_AOS_PACK_OFFLINE_FORM_JSON_PATH
        = "jsonExamples/payloads/bulk/scan/aos/invalidAosPackOfflineForm.json";

    private String validBody;
    private String invalidBody;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthTokenValidator authTokenValidator;

    @Before
    public void setup() throws Exception {
        when(authTokenValidator.getServiceName(ALLOWED_SERVICE_TOKEN)).thenReturn("bulk_scan_orchestrator");
        when(authTokenValidator.getServiceName(I_AM_NOT_ALLOWED_SERVICE_TOKEN)).thenReturn("don't let me do it!");

        validBody = loadResourceAsString(AOS_PACK_OFFLINE_FORM_JSON_PATH);
        invalidBody = loadResourceAsString(INVALID_AOS_PACK_OFFLINE_FORM_JSON_PATH);
    }

    @Test
    public void shouldReturnForbiddenStatusWhenInvalidS2SToken() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(validBody)
                .header(SERVICE_AUTHORISATION_HEADER, I_AM_NOT_ALLOWED_SERVICE_TOKEN)
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnUnauthorisedStatusWhenNoS2SToken() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(validBody)
                .header(SERVICE_AUTHORISATION_HEADER, "")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldReturnNoWarningsForUpdateEndpointWithValidData() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(validBody)
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(
            matchAll(
                status().isOk(),
                content().string(allOf(
                    hasJsonPath("$.warnings", equalTo(emptyList()))
                ))
            ));
    }

    @Test
    public void shouldReturnFailureResponseForValidationEndpoint() throws Exception {
        mockMvc.perform(
            post(UPDATE_URL)
                .contentType(APPLICATION_JSON)
                .content(invalidBody)
                .header(SERVICE_AUTHORISATION_HEADER, ALLOWED_SERVICE_TOKEN)
        ).andExpect(matchAll(
            status().isOk(),
            content().string(allOf(
                hasJsonPath("$.warnings", hasItems(
                    mandatoryFieldIsMissing("RespLegalProceedingsExist"),
                    mandatoryFieldIsMissing("RespConsiderFinancialSituation"),
                    notAValidDate("RespStatementofTruthSignedDate"),
                    mustBeYesOrNo("RespAOS2yrConsent")
                ))
            ))
        ));
    }

    private String mandatoryFieldIsMissing(String fieldName) {
        return String.format("Mandatory field \"%s\" is missing", fieldName);
    }

    private String notAValidDate(String fieldName) {
        return String.format("%s must be a valid 8 digit date", fieldName);
    }


    private String mustBeYesOrNo(String fieldName) {
        return String.format("%s must be \"Yes\" or \"No\"", fieldName);
    }
}