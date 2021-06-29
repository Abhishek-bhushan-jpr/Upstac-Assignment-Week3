package org.upgrad.upstac.testrequests.consultation;


import org.hibernate.engine.spi.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;


@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    Logger log = LoggerFactory.getLogger(ConsultationController.class);




    @Autowired
    private TestRequestUpdateService testRequestUpdateService;

    @Autowired
    private TestRequestQueryService testRequestQueryService;


    @Autowired
    TestRequestFlowService  testRequestFlowService;

    @Autowired
    private UserLoggedInService userLoggedInService;



    @GetMapping("/in-queue")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForConsultations()  {

        // Following code returns the list of Test Reports that have been completed with report & are available as
        // consultation requests.

        try {
            return testRequestQueryService.findBy(RequestStatus.LAB_TEST_COMPLETED);

        }
         catch (AppException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForDoctor()  {

        // Following code returns the list of Consultation Requests with reference to the logged in doctor.

        try {
            User doctor = new User();
            doctor = userLoggedInService.getLoggedInUser();
            return testRequestQueryService.findByDoctor(doctor);
        }
        catch (AppException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

    }


    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/assign/{id}")
    public TestRequest assignForConsultation(@PathVariable Long id) {

        // Following code assigns the selected Test Request to the logged in doctor for Consultation.

        try {
            User doctor = new User();
            doctor = userLoggedInService.getLoggedInUser();
            TestRequest assignTest = new TestRequest();
            return assignTest = testRequestUpdateService.assignForConsultation(id,doctor);
        }

        catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/update/{id}")
    public TestRequest updateConsultation(@PathVariable Long id,@RequestBody CreateConsultationRequest testResult) {

        // Following code updates the consultation remarks from the logged in doctor to the selected test request.

        try {
            User doctor = new User();
            doctor = userLoggedInService.getLoggedInUser();
            TestRequest consultationUpdate = new TestRequest();
            return consultationUpdate = testRequestUpdateService.updateConsultation(id,testResult,doctor);

        } catch (ConstraintViolationException e) {
            throw asConstraintViolation(e);
        }catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }



}
