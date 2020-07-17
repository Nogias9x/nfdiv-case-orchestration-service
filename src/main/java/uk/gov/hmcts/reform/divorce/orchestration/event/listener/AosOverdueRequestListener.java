package uk.gov.hmcts.reform.divorce.orchestration.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueRequest;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationService;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

@Component
@RequiredArgsConstructor
@Slf4j
public class AosOverdueRequestListener implements ApplicationListener<AosOverdueRequest> {

    private final AuthUtil authUtil;
    private final CaseOrchestrationService caseOrchestrationService;

    @Override
    public void onApplicationEvent(AosOverdueRequest aosOverdueRequest) {
        String caseId = aosOverdueRequest.getCaseId();
        log.info("Listened to request to make case [{}] overdue.", caseId);
        caseOrchestrationService.makeCaseAosOverdue(authUtil.getCaseworkerToken(), caseId);
    }

}