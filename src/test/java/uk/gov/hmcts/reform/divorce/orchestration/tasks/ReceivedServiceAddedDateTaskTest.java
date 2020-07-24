package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.prepareTaskContext;

@RunWith(MockitoJUnitRunner.class)
public class ReceivedServiceAddedDateTaskTest extends TestCase {

    @InjectMocks
    private ReceivedServiceAddedDateTask receivedServiceAddedDateTask;

    @Test
    public void executeShouldAddPopulatedField() {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> returnedCaseData = receivedServiceAddedDateTask.execute(prepareTaskContext(), caseData);

        assertThat(returnedCaseData.isEmpty(), is(false));
        assertSame(returnedCaseData, caseData);
        assertThat(
            returnedCaseData.get(ReceivedServiceAddedDateTask.RECEIVED_SERVICE_ADDED_DATE),
            is(DateUtils.formatDateFromLocalDate(LocalDate.now()))
        );
    }
}