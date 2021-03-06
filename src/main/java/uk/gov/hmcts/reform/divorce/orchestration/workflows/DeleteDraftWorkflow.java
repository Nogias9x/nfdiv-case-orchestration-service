package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DeleteDraft;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class DeleteDraftWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final DeleteDraft deleteDraft;

    @Autowired
    public DeleteDraftWorkflow(DeleteDraft deleteDraft) {
        this.deleteDraft = deleteDraft;
    }

    public Map<String, Object> run(String authToken) throws WorkflowException {
        return this.execute(
            new Task[]{
                deleteDraft
            },
            new HashMap<>(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken)
        );
    }
}
