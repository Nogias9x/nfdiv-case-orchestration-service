package uk.gov.hmcts.reform.divorce.orchestration.workflows.aos;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aos.MarkAosCasesAsOverdueTask;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class AosOverdueWorkflowTest {

    @Mock
    private MarkAosCasesAsOverdueTask markCasesToBeMovedToAosOverdue;

    @InjectMocks
    private AosOverdueWorkflow aosOverdueWorkflow;

    @Captor
    private ArgumentCaptor<TaskContext> argumentCaptor;

    @Test
    public void shouldCallAppropriateTasks() throws TaskException, WorkflowException {
        aosOverdueWorkflow.run(AUTH_TOKEN);

        verify(markCasesToBeMovedToAosOverdue).execute(argumentCaptor.capture(), isNull());
        TaskContext context = argumentCaptor.getValue();
        assertThat(context.getTransientObject(AUTH_TOKEN_JSON_KEY), is(AUTH_TOKEN));
    }

}