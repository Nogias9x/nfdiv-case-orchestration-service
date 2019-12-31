package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations.D8FormToCaseTransformer;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;

public class D8FormToCaseTransformerTest {

    private final D8FormToCaseTransformer classUnderTest = new D8FormToCaseTransformer();

    @Test
    public void verifySeparationDateIsTransformed() {
        ExceptionRecord exceptionRecord = createExceptionRecord(singletonList(new OcrDataField("D8ReasonForDivorceSeparationDate", "20/11/2008")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8ReasonForDivorceSeperationDay", "20"),
            hasEntry("D8ReasonForDivorceSeperationMonth", "11"),
            hasEntry("D8ReasonForDivorceSeperationYear", "2008"),
            hasEntry("bulkScanCaseReference", "test_case_id")
        ));
    }

    @Test
    public void shouldReplaceCcdFieldForD8PaymentMethodIfPaymentMethodIsDebitCreditCard() {
        assertIsReplacedBy("Card", "Debit/Credit Card", "D8PaymentMethod");
    }

    @Test
    public void shouldNotReplaceCcdFieldForD8PaymentMethodIfMethodIsCheque() {
        assertIsReplacedBy("Cheque", "Cheque", "D8PaymentMethod");
    }

    @Test
    public void shouldNotReturnUnexpectedField() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(new OcrDataField("UnexpectedName", "UnexpectedValue")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            aMapWithSize(1),
            hasEntry("bulkScanCaseReference", "test_case_id")
        ));
    }

    @Test
    public void shouldReplaceCcdFieldForD8FinancialOrderForIfIsForMyselfMyChildrenOrBoth() {
        assertIsReplacedBy("petitioner", "myself", "D8FinancialOrderFor");
        assertIsReplacedBy("children", "my children", "D8FinancialOrderFor");
        assertIsReplacedBy("petitioner, children", "myself, my children", "D8FinancialOrderFor");
    }


    @Test
    public void verifyPetitionerHomeAddressIsTransformed() {
        assertAddressIsTransformed(
            "D8PetitionerHomeAddress",
            ImmutableMap.of(
                "D8PetitionerHomeAddressStreet", "19 West Park Road",
                "D8PetitionerHomeAddressTown", "Smethwick",
                "D8PetitionerPostCode", "SE1 2PT",
                "D8PetitionerHomeAddressCounty", "West Midlands"
            ));
    }

    @Test
    public void verifyPetitionerSolicitorAddressIsTransformed() {
        assertAddressIsTransformed(
            "PetitionerSolicitorAddress",
            ImmutableMap.of(
                "PetitionerSolicitorAddressStreet", "20 solicitors road",
                "PetitionerSolicitorAddressTown", "Soltown",
                "PetitionerSolicitorAddressPostCode", "SE1 2PT",
                "PetitionerSolicitorAddressCounty", "East Midlands"
            ));
    }

    @Test
    public void verifyD8PetitionerCorrespondenceAddressIsTransformed() {
        assertAddressIsTransformed(
            "D8PetitionerCorrespondenceAddress",
            ImmutableMap.of(
                "D8PetitionerCorrespondenceAddressStreet", "20 correspondence road",
                "D8PetitionerCorrespondenceAddressTown", "Correspondencetown",
                "D8PetitionerCorrespondencePostcode", "SE12BP",
                "D8PetitionerCorrespondenceAddressCounty", "South Midlands"
            ));
    }

    @Test
    public void verifyD8ReasonForDivorceAdultery3rdPartyAddressIsTransformed() {
        assertAddressIsTransformed(
            "D8ReasonForDivorceAdultery3rdAddress",
            ImmutableMap.of(
                "D8ReasonForDivorceAdultery3rdPartyAddressStreet", "third party street",
                "D8ReasonForDivorceAdultery3rdPartyTown", "Thirdtown",
                "D8ReasonForDivorceAdultery3rdPartyPostCode", "SE3 5PP",
                "D8ReasonForDivorceAdultery3rdPartyCounty", "North Midlands"
            ));
    }

    private void assertIsReplacedBy(String expectedValue, String inputValue, String field) {
        ExceptionRecord exceptionRecord =
            createExceptionRecord(singletonList(new OcrDataField(field, inputValue)));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData.get(field), is(expectedValue));
    }

    @Test
    public void verifyMarriageDateIsCorrectlyTransformedGivenSeparateComponents() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("D8MarriageDateDay", "06"),
            new OcrDataField("D8MarriageDateMonth", "5"),
            new OcrDataField("D8MarriageDateYear", "2007")
        ));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8MarriageDate", "2007-05-06"),
            not(hasKey("D8MarriageDateDay")),
            not(hasEntry("D8MarriageDate", "2007-5-6")),
            not(hasEntry("D8MarriageDate", "2007-06-05"))
        ));
    }

    @Test
    public void verifySection3RespondentIsTransformed() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("D8PetitionerNameDifferentToMarriageCert", "Yes"),
            new OcrDataField("RespNameDifferentToMarriageCertExplain", "Dog ate the homework"),
            new OcrDataField("D8RespondentEmailAddress", "jack@daily.mail.com"),
            new OcrDataField("D8RespondentCorrespondenceSendToSol", "Yes"),
            new OcrDataField("D8RespondentSolicitorName", "Judge Law"),
            new OcrDataField("D8RespondentSolicitorReference", "JL007"),
            new OcrDataField("D8RespondentSolicitorCompany", "A-Team")
        ));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry("D8PetitionerNameDifferentToMarriageCert", "Yes"),
            hasEntry("RespNameDifferentToMarriageCertExplain", "Dog ate the homework"),
            hasEntry("D8RespondentEmailAddress", "jack@daily.mail.com"),
            hasEntry("D8RespondentCorrespondenceSendToSol", "Yes"),
            hasEntry("D8RespondentSolicitorName", "Judge Law"),
            hasEntry("D8RespondentSolicitorReference", "JL007"),
            hasEntry("D8RespondentSolicitorCompany", "A-Team")
        ));
    }

    @Test
    public void verifyD8RespondentHomeAddressIsTransformed() {
        assertAddressIsTransformed(
            "D8RespondentHomeAddress",
            ImmutableMap.of(
                "D8RespondentHomeAddressStreet", "12 Res Pond",
                "D8RespondentHomeAddressTown", "Divorcity",
                "D8RespondentPostcode", "RE5 3NT",
                "D8RespondentHomeAddressCounty", "Mid Midlands"
            ));
    }

    @Test
    public void verifyD8RespondentSolicitorAddressIsTransformed() {
        assertAddressIsTransformed(
            "D8RespondentSolicitorAddress",
            ImmutableMap.of(
                "D8RespondentSolicitorAddressStreet", "50 Licitor",
                "D8RespondentSolicitorAddressTown", "Lawyerpool",
                "D8RespondentSolicitorAddressPostCode", "SO2 7OR",
                "D8RespondentSolicitorAddressCounty", "Higher Midlands"
            ));
    }

    private ExceptionRecord createExceptionRecord(List<OcrDataField> ocrDataFields) {
        return ExceptionRecord.builder().id("test_case_id").ocrDataFields(ocrDataFields).build();
    }

    private void assertAddressIsTransformed(String targetParentField, Map<String, String> sourceFieldAndValueMap) {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(sourceFieldAndValueMap.entrySet().stream()
            .map(fieldAndValue -> new OcrDataField(fieldAndValue.getKey(), fieldAndValue.getValue()))
            .collect(Collectors.toList()));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(incomingExceptionRecord);

        Map<String, String> sourceSuffixToTargetMap = ImmutableMap.of(
            "AddressStreet", "AddressLine1",
            "Town", "PostTown",
            "PostCode", "PostCode",
            "County", "County"
        );
        BiFunction<Map<String, String>, String, String> getValueForSuffix = (map, suffix) -> map.entrySet().stream()
            .filter(entry -> entry.getKey().toLowerCase().endsWith(suffix.toLowerCase()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(String.format("Expected to find key with suffix %s in map", suffix)))
            .getValue();

        Map parentField = (Map) transformedCaseData.getOrDefault(targetParentField, emptyMap());
        sourceSuffixToTargetMap.entrySet().forEach(suffixToTarget ->
            assertThat(parentField.get(suffixToTarget.getValue()), is(getValueForSuffix.apply(sourceFieldAndValueMap, suffixToTarget.getKey())))
        );
    }
}
