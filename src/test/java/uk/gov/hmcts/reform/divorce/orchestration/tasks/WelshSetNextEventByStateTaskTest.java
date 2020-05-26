package uk.gov.hmcts.reform.divorce.orchestration.tasks;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;

@RunWith(MockitoJUnitRunner.class)
public class WelshSetNextEventByStateTaskTest {
    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @InjectMocks
    private WelshSetNextEventByStateTask welshSetNextEventByStateTask;

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
    public void testExecuteSuccess() throws TaskException {
        final Map<String, Object> divorceSession = getCaseData(false, true);

        CaseDetails caseDetails = CaseDetails.builder().caseData(divorceSession).build();
        when(caseMaintenanceClient.retrievePetitionById(AUTH_TOKEN, TEST_CASE_ID)).thenReturn(caseDetails);


        welshSetNextEventByStateTask.execute(context, caseData);
        verify(caseMaintenanceClient).updateCase(eq(context.getTransientObject(AUTH_TOKEN_JSON_KEY)),
            eq(context.getTransientObject(CASE_ID_JSON_KEY)),same("Continue"),
            eq(caseData));
        assertThat(caseData).containsEntry(WELSH_NEXT_EVENT, null);
    }
}