package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_TRANSISTION_TO_PENDING_REJECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_TRANSISTION_TO_SUBMITTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PENDING_REJECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUBMITTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UPDATE_NEXT_WELSH_CASE_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_PREVIOUS_STATE;

@RequiredArgsConstructor
@Component
public class WelshSetNextEventByStateTask implements Task<Map<String, Object>> {
    private final CaseMaintenanceClient caseMaintenanceClient;

    @Override
    public Map<String, Object> execute(TaskContext context, final Map<String, Object> payload) throws TaskException {
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String caseIDJsonKey = context.getTransientObject(CASE_ID_JSON_KEY);

        String previousState = Optional.ofNullable(payload.get(WELSH_PREVIOUS_STATE)).map(String.class::cast).get();

        payload.put(WELSH_NEXT_EVENT, getNextEventId(previousState));
        payload.put(WELSH_PREVIOUS_STATE, null);
        try {
            caseMaintenanceClient.updateCase(
                    authToken,
                    caseIDJsonKey,
                    UPDATE_NEXT_WELSH_CASE_EVENT,
                    payload
            );
        } catch (FeignException exception) {
            payload.put(WELSH_PREVIOUS_STATE, previousState);
            throw new TaskException(String.join(" ", "For case:", caseIDJsonKey, "update failed for event id", UPDATE_NEXT_WELSH_CASE_EVENT), exception);
        }

       return payload;
    }

    private String getNextEventId(final String currentState) {
       switch (currentState) {
            case SUBMITTED :
                return BO_TRANSISTION_TO_SUBMITTED;
            case PENDING_REJECTION :
                return BO_TRANSISTION_TO_PENDING_REJECTION;
            default: throw new IllegalArgumentException(String.join(" ","No event set for:", currentState));
        }
    }
}
