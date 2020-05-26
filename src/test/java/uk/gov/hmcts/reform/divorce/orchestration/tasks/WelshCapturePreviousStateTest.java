package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_PREVIOUS_STATE;

@RunWith(MockitoJUnitRunner.class)
public class WelshCapturePreviousStateTest {
    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private WelshCapturePreviousState welshCapturePreviousState;

    private TaskContext context;
    private Map<String, Object> caseData;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
        caseData = new HashMap<>();
        caseData.put(WELSH_NEXT_EVENT, "Continue");
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, "KEY");
        context.setTransientObject(CASE_ID_JSON_KEY, "CASEID");
    }

    @Test
    public void testExecute() throws TaskException {
        CaseDetails caseDetails = CaseDetails.builder().state("previousState").build();
        when(caseMaintenanceClient.retrievePetitionById(eq(context.<String>getTransientObject(AUTH_TOKEN_JSON_KEY))
            , eq(context.<String>getTransientObject(CASE_ID_JSON_KEY))))
            .thenReturn(caseDetails);
        Map<String, Object> payload = welshCapturePreviousState.execute(context, caseData);
        assertThat(payload, hasEntry(WELSH_PREVIOUS_STATE, "previousState"));
    }
}