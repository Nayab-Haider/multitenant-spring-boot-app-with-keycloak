package com.demo.api.configuration;

import lombok.Data;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

@Data
public class CustomUserDetails  {

    private String tenant;

    public CustomUserDetails(String username, String password,
                             Collection<? extends GrantedAuthority> authorities, String tenant) {
//        super(username, password, authorities);
        this.tenant = tenant;
    }

    public static class SecurityConfig {

        private final Logger log = LoggerFactory.getLogger(SecurityConfig.class);


        @Configuration
        @EnableWebSecurity
        @ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true", matchIfMissing = true)
        @ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
        public static class KeycloakConfigurationAdapter extends KeycloakWebSecurityConfigurerAdapter {

            @Autowired
            public SimpleCORSFilter simpleCORSFilter;
            /**
             * Registers the KeycloakAuthenticationProvider with the authentication manager.
             */
            @Autowired
            public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
                KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
                // simple Authority Mapper to avoid ROLE_
                keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
                auth.authenticationProvider(keycloakAuthenticationProvider);
            }

            @Bean
            public KeycloakConfigResolver KeycloakConfigResolver() {
                return new HeaderBasedConfigResolver();
            }

            /**
             * Defines the session authentication strategy.
             */
            @Bean
            @Override
            protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
                // required for bearer-only applications.
                return new NullAuthenticatedSessionStrategy();
            }

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http
                        .addFilterBefore(simpleCORSFilter, ChannelProcessingFilter.class)
                        .sessionManagement()
                        // use previously declared bean
                        .sessionAuthenticationStrategy(sessionAuthenticationStrategy())


                        // keycloak filters for securisation
                        .and()
                        .addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class)
                        .addFilterBefore(keycloakAuthenticationProcessingFilter(), X509AuthenticationFilter.class)
                        .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())

                        // add cors options
                        .and().cors()
                        // delegate logout endpoint to spring security

                        .and()
                        .logout()
                        .addLogoutHandler(keycloakLogoutHandler())
                        .logoutUrl("/logout").logoutSuccessHandler(
                        // logout handler for API
                        (HttpServletRequest request, HttpServletResponse response, Authentication authentication) ->
                                response.setStatus(HttpServletResponse.SC_OK)
                )
                        .and().apply(new CommonSpringKeycloakSecuritAdapter());
            }

    //        @Bean
    //        public CorsConfigurationSource corsConfigurationSource() {
    //            CorsConfiguration configuration = new CorsConfiguration();
    //            configuration.setAllowedOrigins(Arrays.asList("*"));
    //            configuration.setAllowedMethods(Arrays.asList(HttpMethod.OPTIONS.name(), "POST, GET, OPTIONS, DELETE,PUT"));
    //            configuration.setAllowedHeaders(Arrays.asList("Access-Control-Allow-Headers", "Access-Control-Allow-Origin", "Authorization"));
    //            configuration.setAllowCredentials(true);
    //            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //            source.registerCorsConfiguration("/**", configuration);
    //            return source;
    //        }

        }


        public static class CommonSpringKeycloakSecuritAdapter extends AbstractHttpConfigurer<CommonSpringKeycloakSecuritAdapter, HttpSecurity> {

            @Override
            public void init(HttpSecurity http) throws Exception {
                // any method that adds another configurer
                // must be done in the init method
                http
                        // disable csrf because of API mode
                        .cors().and().csrf().disable()
                        .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                        .and()
                        // manage routes securisation here
                        .authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()

                        // manage routes securisation here
                        .and()
                        .authorizeRequests()
                        .antMatchers(HttpMethod.OPTIONS).permitAll()


                        .antMatchers(HttpMethod.GET, "/v1/dashboard/attendanceScore/{empCode}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/dashboard/employeeconnectevents").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/dashboard/saveQuickLinksEmployee").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/dashboard/{empCode}/expensedetails").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/dashboard/{empCode}/getQuickLinksEmployee").hasAnyRole("user",
                        "admin")
                        .antMatchers(HttpMethod.GET, "/v1/dashboard/{empCode}/getQuickLinksMaster").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/dashboard/{empCode}/leavescore").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/notifications/{empCode}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/notifications/{empCode}/unread").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/notifications/{notificationId}/read").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/attendance/attendanceRecords/").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/alerts/pendingRequest/{empCode}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/employee/").hasAnyRole("user", "viewSensitiveDataAdmin",
                        "restrictAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/document/policy/{empCode}/{role}").hasAnyRole("user",
                        "viewSensitiveDataAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/documents/generatedLetters/{empCode}/{role}").hasAnyRole
                        ("user", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/employee/profile/{empCode}/uploadFile").hasAnyRole("user",
                        "editdelEmpAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/employee/profile/{empCode}/header").hasAnyRole("user",
                        "restrictAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/employee/profile/{empCode}/{profileLocation}").hasAnyRole("user",
                        "viewSensitiveDataAdmin", "editdelEmpAdmin",
                        "admin")
                        .antMatchers(HttpMethod.GET, "/v1/employee/salary/{empCode}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/payroll/payslip/history/{empCode}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/employee/profile/").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/calendar/{startDate}/{endDate}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/leave/settings/leaveAssignments/{empCode}").hasAnyRole("user",
                        "admin")
                        .antMatchers(HttpMethod.GET, "/v1/organization/holiday/").hasAnyRole("user",
                        "addEditdelCompanyHolidayAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/organization/employeetree/").hasAnyRole("user",
                        "admin")
                        .antMatchers(HttpMethod.GET, "/v1/document/{empCode}/{role}").hasAnyRole("user",
                        "admin")
                        .antMatchers(HttpMethod.GET, "/v1/organization/holiday/{calendarYear}/{empCode}").hasAnyRole("user",
                        "viewSensitiveDataAdmin", "empAdmin", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/leave/leaveapplication/").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/leave/leaveapplication/{leaveId}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/leave/leaveapplication/template/{empCode}").hasAnyRole("user",
                        "admin")
                        .antMatchers(HttpMethod.GET, "/v1/leave/leaveapplication/{empCode}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/leave/leaveapplication/{leaveId}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/leave/compoffEarnings/applyCompOffByUser").hasAnyRole("user",
                        "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/leave/compoffEarnings/compOffActionByUser/{compoffId}/action")
                        .hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/leave/compoffEarnings/getCompOffBalanceByEmpCode/{empCode}")
                        .hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/leave/balance/leaveCycle").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/leave/balance/{empCode}/{leaveCycle}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/leave/leaveapplication/employee/{empCode}").hasAnyRole("user",
                        "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/leave/compoffEarnings/compOffActionBySupervisor/{compoffId}/action" +
                                "").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/leave/compoffEarnings/updateCompOffBySupervisor/{compoffId}")
                        .hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/leave/compoffEarnings/getAllCompOffBySupervisor/{empCode}")
                        .hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/leave/compoffEarnings/getAllCompOffByEmpCode/{empCode}")
                        .hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/leave/leaveapplication/supervisor/{empCode}").hasAnyRole("user",
                        "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/leave/leaveapplication/{leaveId}/action").hasAnyRole("user")
                        .antMatchers(HttpMethod.GET, "/v1/attendance/attendanceRecords/{empCode}/{monthYear}").hasAnyRole
                        ("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/attendance/attendanceRecords/getMarkAttendance/{empCode}")
                        .hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/attendance/attendanceRecords/attendanceMonthYearList").hasAnyRole
                        ("user", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/attendance/regularization/").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/attendance/settings/regularizationReason/{empCode}").hasAnyRole
                        ("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/attendance/regularization/{empCode}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/attendance/regularization/{regularizationId}").hasAnyRole("user",
                        "admin")
                        .antMatchers(HttpMethod.GET, "/v1/attendance/attendanceRecords/teamRecords/{empCode}/{monthYear}")
                        .hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/attendance/regularization/supervisor/{empCode}").hasAnyRole("user",
                        "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/attendance/regularization/{regularizationId}/action").hasAnyRole
                        ("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/expense/generalinfo/{empCode}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/expense/application/{empCode}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/expense/application/").hasAnyRole("user")
                        .antMatchers(HttpMethod.GET, "/v1/expense/application/expensecategories/{empCode}").hasAnyRole
                        ("user", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/expense/application/{expenseId}").hasAnyRole("user")
                        .antMatchers(HttpMethod.PUT, "/v1/expense/application/{expenseId}/action").hasAnyRole("user")
                        .antMatchers(HttpMethod.GET, "/v1/expense/application/employee/{empCode}").hasAnyRole("user", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/expense/application/supervisor/{empCode}").hasAnyRole("user", "admin")
                        //for resignation of user
                        .antMatchers(HttpMethod.POST, "/v1/resignation/application/").hasAnyRole("user")
                        .antMatchers(HttpMethod.GET, "/v1/resignation/application/supervisor/{empCode}").hasAnyRole("user")
                        .antMatchers(HttpMethod.GET, "/v1/resignation/application/{empCode}").hasAnyRole("user")
                        .antMatchers(HttpMethod.DELETE, "/v1/resignation/application/{resignationId}").hasAnyRole("user")
                        .antMatchers(HttpMethod.PUT, "/v1/resignation/application/{resignationId}").hasAnyRole("user")
                        .antMatchers(HttpMethod.PUT, "/v1/resignation/application/{resignationId}/action").hasAnyRole("user")


                        //THIS RULE IS USED IN MY REGULARIZATION REQUESTS
                        .antMatchers(HttpMethod.GET, "/v1/attendance/settings/general/").hasAnyRole("user", "admin")
    //NEW SET OF RULES FOR USER ROLE ENDS HERE

                        //RULE FOR RESTRICT ADMIN ROLE STARTS HERE

                        //Organization STARTS
                        .antMatchers(HttpMethod.GET, "/v1/organization/**").hasAnyRole("orgAdmin", "payrollAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/document/**").hasAnyRole("orgAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/documents/lettertemplates/**").hasAnyRole("orgAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/documents/generatedLetters/**").hasAnyRole("orgAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/documents/generatedLetters/generate").hasAnyRole
                        ("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/organization/employeefields/section").hasAnyRole
                        ("viewSensitiveDataAdmin", "empAdmin", "orgAdmin", "admin")

                        .antMatchers(HttpMethod.PUT, "/v1/organization/orgAddress/{id}").hasAnyRole("editOrgDetailsAdmin",
                        "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/organization/orgBasicInfo/{id}").hasAnyRole("editOrgDetailsAdmin",
                        "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/organization/orgBankInfo/{id}").hasAnyRole("editOrgDetailsAdmin",
                        "admin")
                        .antMatchers(HttpMethod.POST, "/v1/organization/orgBankInfo").hasAnyRole("editOrgDetailsAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/organization/orgBankInfo/{id}").hasAnyRole
                        ("editOrgDetailsAdmin", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/organization/orgLocation").hasAnyRole("editOrgDetailsAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/organization/orgLocation/{id}").hasAnyRole("editOrgDetailsAdmin",
                        "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/organization/orgLocation/{id}").hasAnyRole
                        ("editOrgDetailsAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/organization/orgSignatoryInfo/{id}").hasAnyRole
                        ("editOrgDetailsAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/organization/manageadmin/").hasAnyRole("addEditRemoveOtherAdmin",
                        "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/organization/manageadmin/{adminId}").hasAnyRole
                        ("addEditRemoveOtherAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/organization/manageadmin/{adminId}").hasAnyRole
                        ("addEditRemoveOtherAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/document/policy/").hasAnyRole("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/document/policy/{empDocRecordId}").hasAnyRole
                        ("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/document/policy/{empDocRecordId}").hasAnyRole
                        ("generateLetterAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/document/category").hasAnyRole("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/document/{empDocRecordId}").hasAnyRole("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/document/{empDocRecordId}").hasAnyRole("generateLetterAdmin",
                        "admin")

                        .antMatchers(HttpMethod.POST, "/v1/documents/lettertemplates/letterTemplate").hasAnyRole
                        ("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/documents/lettertemplates/letterTemplate/{letterTemplateId}")
                        .hasAnyRole("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/documents/lettertemplates/letterTemplate/{letterTemplateId}")
                        .hasAnyRole("generateLetterAdmin",
                                "admin")

                        .antMatchers(HttpMethod.POST, "/v1/documents/lettertemplates/headerTemplates").hasAnyRole
                        ("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/documents/lettertemplates/headerTemplates/{letterHeaderTemplateId" +
                                "}").hasAnyRole("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE,
                                "/v1/documents/lettertemplates/headerTemplates/{letterHeaderTemplateId}").hasAnyRole
                        ("generateLetterAdmin",
                                "admin")

                        .antMatchers(HttpMethod.POST, "/v1/documents/lettertemplates/footerTemplate").hasAnyRole
                        ("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/documents/lettertemplates/footerTemplate/{letterFooterTemplateId}")
                        .hasAnyRole("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE,
                                "/v1/documents/lettertemplates/footerTemplate/{letterFooterTemplateId}").hasAnyRole
                        ("generateLetterAdmin",
                                "admin")

                        .antMatchers(HttpMethod.POST, "/v1/documents/lettertemplates/authorizedSignatory").hasAnyRole
                        ("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/documents/lettertemplates/authorizedSignatory").hasAnyRole
                        ("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE,
                                "/v1/documents/lettertemplates/authorizedSignatory/{authSignatoryId}").hasAnyRole
                        ("generateLetterAdmin",
                                "admin")

                        .antMatchers(HttpMethod.POST, "/v1/documents/generatedLetters/generate").hasAnyRole
                        ("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/documents/generatedLetters/publishById/{documentId}/{status}")
                        .hasAnyRole("generateLetterAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/documents/generatedLetters/{documentId}").hasAnyRole
                        ("generateLetterAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/organization/employeefields/section").hasAnyRole
                        ("editEmpFieldAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/organization/employeefields/section/{sectionId}")
                        .hasAnyRole("editEmpFieldAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/organization/employeefields/section/{sectionId}").hasAnyRole
                        ("editEmpFieldAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/organization/employeefields/field").hasAnyRole
                        ("editEmpFieldAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/organization/employeefields/field/{fieldId}")
                        .hasAnyRole("editEmpFieldAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/organization/employeefields/field/{fieldId}").hasAnyRole
                        ("editEmpFieldAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/organization/holiday/").hasAnyRole
                        ("addEditdelCompanyHolidayAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/organization/holiday/{holidayId}").hasAnyRole
                        ("addEditdelCompanyHolidayAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/organization/holiday/{holidayId}").hasAnyRole
                        ("addEditdelCompanyHolidayAdmin", "admin")
                        //Organization ENDS

                        //EMPLOYEE STARTS
                        .antMatchers(HttpMethod.GET, "/v1/employee/user").hasAnyRole("empAdmin", "payrollAdmin", "admin", "user")

                        .antMatchers(HttpMethod.GET, "/v1/employee/**").hasAnyRole("empAdmin", "payrollAdmin", "admin")

                        .antMatchers(HttpMethod.DELETE, "/v1/employee/{empCode}").hasAnyRole("editdelEmpAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/employee/profile/admin/").hasAnyRole("user", "editdelEmpAdmin",
                        "editSensitiveDataAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/employee/salary/{recordNo}").hasAnyRole("editdelEmpAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/employee/salary/{recordNo}").hasAnyRole("editdelEmpAdmin",
                        "editSensitiveDataAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/employee/onboard/").hasAnyRole("addEmpAdmin", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/employee/salary/").hasAnyRole("addEmpAdmin", "editdelEmpAdmin",
                        "assignCTCTempAdmin",
                        "admin")
                        .antMatchers(HttpMethod.GET, "/v1/employee/{empCode}/invite").hasAnyRole("editdelEmpAdmin",
                        "addEmpAdmin",
                        "admin")

                        .antMatchers(HttpMethod.GET, "/v1/employee/{empCode}/hold").hasAnyRole("viewSensitiveDataAdmin",
                        "admin")

                        .antMatchers(HttpMethod.GET, "/v1/employee/{empCode}/terminate").hasAnyRole("viewSensitiveDataAdmin",
                        "admin")
                        .antMatchers(HttpMethod.GET, "/v1/employee/onboard/getFields/{profileLocation}").hasAnyRole
                        ("viewSensitiveDataAdmin", "editdelEmpAdmin", "empAdmin", "addEmpAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/employee/salary/salaryInfo/{recordNo}").hasAnyRole
                        ("viewSensitiveDataAdmin", "empAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/employee/salary/satutoryInfo/{empCode}").hasAnyRole
                        ("viewSensitiveDataAdmin", "empAdmin", "editdelEmpAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/employee/salary/admin/{empCode}").hasAnyRole("viewSensitiveDataAdmin",
                        "empAdmin",
                        "editdelEmpAdmin", "assignCTCTempAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/document/admin/{empCode}/{role}").hasAnyRole("viewSensitiveDataAdmin",
                        "empAdmin",
                        "editdelEmpAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/documents/generatedLetters/admin/{empCode}/{role}").hasAnyRole
                        ("viewSensitiveDataAdmin", "empAdmin", "editdelEmpAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/document/policy/{empDocRecordId}").hasAnyRole
                        ("viewSensitiveDataAdmin", "empAdmin", "admin")

                        //EMPLOYEE ENDS

                        //ATTENDANCE STARTS
                        .antMatchers(HttpMethod.GET, "/v1/attendance/**").hasAnyRole("attendanceAdmin", "admin")

                        .antMatchers(HttpMethod.PUT, "/v1/attendance/regularization/admin/{regularizationId}/action")
                        .hasAnyRole("modifyAttdRecordsAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/attendance/regularization/admin/{regularizationId}").hasAnyRole
                        ("modifyAttdRecordsAdmin", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/attendance/regularization/admin/").hasAnyRole
                        ("modifyAttdRecordsAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/attendance/regularization/{regularizationId}").hasAnyRole
                        ("modifyAttdRecordsAdmin", "admin", "user")

                        .antMatchers(HttpMethod.POST, "/v1/attendance/settings/templates/").hasAnyRole
                        ("modifyAttdTempGenralSettingAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/attendance/settings/templates/{templateId}").hasAnyRole
                        ("modifyAttdTempGenralSettingAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/attendance/settings/templates/{templateId}").hasAnyRole
                        ("modifyAttdTempGenralSettingAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/attendance/settings/templates/regularization")
                        .hasAnyRole("modifyAttdTempGenralSettingAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/attendance/settings/templates/regularization/{regularizationId}")
                        .hasAnyRole("modifyAttdTempGenralSettingAdmin", "admin")


                        .antMatchers(HttpMethod.PUT, "/v1/attendance/settings/general/{regId}").hasAnyRole
                        ("modifyAttdTempGenralSettingAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/attendance/settings/regularizationReason/").hasAnyRole
                        ("modifyAttdTempGenralSettingAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/attendance/settings/regularizationReason/{reasonId}").hasAnyRole
                        ("modifyAttdTempGenralSettingAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/attendance/settings/regularizationReason/{reasonId}").hasAnyRole
                        ("modifyAttdTempGenralSettingAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/attendance/settings/templateAssignments/").hasAnyRole
                        ("assignAttdTempAdmin", "addEmpAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/attendance/settings/templateAssignments/{attdTemplateId}").hasAnyRole
                        ("assignAttdTempAdmin", "addEmpAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/attendance/settings/templateAssignments/{attdTemplateId}")
                        .hasAnyRole("assignAttdTempAdmin", "addEmpAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/attendance/settings/shift/").hasAnyRole
                        ("editAttdShiftAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/attendance/settings/shift/{shiftRecordId}").hasAnyRole
                        ("editAttdShiftAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/attendance/settings/shift/{shiftRecordId}").hasAnyRole
                        ("editAttdShiftAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/attendance/settings/shift/assignment/").hasAnyRole
                        ("assignAttdShiftTempAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/attendance/settings/shift/assignment/{shiftAssignRecordId}")
                        .hasAnyRole
                                ("assignAttdShiftTempAdmin", "admin")
                        //ATTENDANCE ENDS

                        //LEAVE STARTS
                        .antMatchers(HttpMethod.GET, "/v1/leave/**").hasAnyRole("leaveAdmin", "admin")

                        .antMatchers(HttpMethod.PUT, "/v1/leave/settings/general/{id}").hasAnyRole
                        ("modifyLeaveTempSettingAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/leave/settings/leaveCategories").hasAnyRole
                        ("modifyLeaveTempSettingAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/leave/settings/leaveCategories/{categoryId}").hasAnyRole
                        ("modifyLeaveTempSettingAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/leave/settings/leaveCategories/{categoryId}").hasAnyRole
                        ("modifyLeaveTempSettingAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/leave/settings/leaveTemplate").hasAnyRole
                        ("modifyLeaveTempSettingAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/leave/settings/leaveTemplate/{leaveTemplateId}").hasAnyRole
                        ("modifyLeaveTempSettingAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/leave/settings/leaveTemplate/{leaveTemplateId}").hasAnyRole
                        ("modifyLeaveTempSettingAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/leave/settings/leaveTemplateCategory")
                        .hasAnyRole("modifyLeaveTempSettingAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/leave/settings/leaveTemplateCategory/{leaveTemplateCategoryId}")
                        .hasAnyRole("modifyLeaveTempSettingAdmin", "admin")


                        .antMatchers(HttpMethod.POST, "/v1/leave/settings/leaveAssignments/").hasAnyRole
                        ("assignLeaveTempAdmin", "addEmpAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/leave/settings/leaveAssignments/{templateId}").hasAnyRole
                        ("assignLeaveTempAdmin", "addEmpAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/leave/settings/leaveAssignments/{templateId}").hasAnyRole
                        ("assignLeaveTempAdmin", "addEmpAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/leave/leaveapplication/admin/").hasAnyRole
                        ("leaveCancellationCrudAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/leave/leaveapplication/admin/{leaveId}").hasAnyRole
                        ("leaveCancellationCrudAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/leave/leaveapplication/admin/{leaveId}").hasAnyRole
                        ("leaveCancellationCrudAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/leave/leaveapplication/admin/{leaveId}/action").hasAnyRole
                        ("leaveCancellationCrudAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/leave/compoffEarnings/deleteCompOffByAdmin/{compOffId}")
                        .hasAnyRole("leaveCancellationCrudAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/leave/compoffEarnings/updateCompOffByAdmin/{compoffId}").hasAnyRole
                        ("leaveCancellationCrudAdmin", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/leave/compoffEarnings/grantCompOffByAdmin").hasAnyRole
                        ("leaveCancellationCrudAdmin", "admin")
                        //LEAVE ENDS

                        //EXPENSE STARTS
                        .antMatchers(HttpMethod.GET, "/v1/expense/**").hasAnyRole("expenseAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/expense/application/admin/").hasAnyRole("expenseReportCrudAdmin",
                        "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/expense/application/admin/{expenseId}").hasAnyRole
                        ("expenseReportCrudAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/expense/application/{expenseId}").hasAnyRole
                        ("expenseReportCrudAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/expense/application/admin/{expenseId}/action").hasAnyRole
                        ("expenseReportCrudAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/expense/").hasAnyRole
                        ("modifyExpenseTempAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/expense/{categoryId}").hasAnyRole
                        ("modifyExpenseTempAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/expense/settings/expenseTemplate").hasAnyRole
                        ("modifyExpenseTempAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/expense/settings/expenseTemplate/{expenseTemplateId}").hasAnyRole
                        ("modifyExpenseTempAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/expense/settings/expenseTemplate/{expenseTemplateId}").hasAnyRole
                        ("modifyExpenseTempAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/expense/settings/expenseTemplateCategory").hasAnyRole
                        ("modifyExpenseTempAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/expense/settings/expenseTemplateCategory/{expenseTemplateCategoryId" +
                                "}").hasAnyRole("modifyExpenseTempAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/expense/settings/expenseassignments/").hasAnyRole
                        ("assignExpenseTempAdmin", "addEmpAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/expense/settings/expenseassignments//{templateId}").hasAnyRole
                        ("assignExpenseTempAdmin", "addEmpAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/expense/settings/expenseassignments/").hasAnyRole
                        ("assignExpenseTempAdmin", "addEmpAdmin", "admin")
                        //EXPENSE ENDS

                        //CALENDAR STARTS
                        .antMatchers(HttpMethod.GET, "/v1/calendar/**").hasAnyRole("calendarAdmin", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/calendar/milestone/").hasAnyRole("addEditdelMilestonsAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/calendar/milestone/{eventId}").hasAnyRole
                        ("addEditdelMilestonsAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/calendar/milestone/{eventId}").hasAnyRole
                        ("addEditdelMilestonsAdmin", "admin")
                        //CALENDAR ENDS

                        //PAYROLL STARTS
                        .antMatchers(HttpMethod.GET, "/v1/payroll/settings/**").hasAnyRole("payrollAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/payroll/ctc/template/**").hasAnyRole("payrollAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/payroll/runpayroll/**").hasAnyRole("payrollAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/payroll/payslip/**").hasAnyRole("payrollAdmin", "admin")

                        .antMatchers(HttpMethod.PUT, "/v1/payroll/settings/general").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/payroll/settings/allowances/fixed").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/payroll/settings/allowances/fixed/{allowanceId}").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/payroll/settings/allowances/fixed/{allowanceId}").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/payroll/settings/allowances/variable").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/payroll/settings/allowances/variable/{allowanceId}").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/payroll/settings/allowances/variable/{allowanceId}").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/payroll/settings/deductions/fixed").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/payroll/settings/deductions/fixed/{deductionId}").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/payroll/settings/deductions/fixed/{deductionId}").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/payroll/settings/deductions/variable").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/payroll/settings/deductions/variable/{deductionId}").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/payroll/settings/deductions/variable/{deductionId}").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/payroll/ctc/template/").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/payroll/ctc/template/{ctcTemplateId}").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/payroll/ctc/template/{ctcTemplateId}").hasAnyRole
                        ("modifyPayrollSettingCTCTempAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/payroll/runpayroll/").hasAnyRole
                        ("runPayrollAdmin", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/payroll/runpayroll/rejectionOrApprover/{status}").hasAnyRole
                        ("runPayrollAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/payroll/runpayroll/{runPayrollStatus}").hasAnyRole
                        ("runPayrollAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/payroll/runpayroll/details/{runPayrollEmployeeStatusId}").hasAnyRole
                        ("runPayrollAdmin", "admin")

                        .antMatchers(HttpMethod.POST, "/v1/payroll/payslip/publish/{status}").hasAnyRole
                        ("publishPaymentsAdmin", "admin")


                        //PAYROLL ENDS

                        //START for resignation of ADMIN
                        .antMatchers(HttpMethod.GET, "/v1/resignation/application/").hasAnyRole("resignationAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/resignation/application/admin/{resignationId}").hasAnyRole("resignationCancellationCrudAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/resignation/application/admin/{resignationId}/action").hasAnyRole("resignationCancellationCrudAdmin", "admin")

                        .antMatchers(HttpMethod.GET, "/v1/resignation/settings/resignationTemplate").hasAnyRole("resignationAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/resignation/settings/resignationTemplate/{resignationTemplateId}").hasAnyRole("modifyResignationTempAdmin", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/resignation/settings/resignationTemplate").hasAnyRole("modifyResignationTempAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/resignation/settings/resignationTemplate/{resignationTemplateId}").hasAnyRole("modifyResignationTempAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/resignation/settings/resignationTemplate/{resignationTemplateId}").hasAnyRole("modifyResignationTempAdmin", "admin")

                        .antMatchers(HttpMethod.GET, "/v1/resignation/settings/resignationAssignments/").hasAnyRole("resignationAdmin", "admin")
                        .antMatchers(HttpMethod.GET, "/v1/resignation/settings/resignationAssignments/{empCode}").hasAnyRole("assignResignationTempAdmin", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/resignation/settings/resignationAssignments/").hasAnyRole("assignResignationTempAdmin", "admin")
                        .antMatchers(HttpMethod.PUT, "/v1/resignation/settings/resignationAssignments/{resignationTemplateId}").hasAnyRole("assignResignationTempAdmin", "admin")
                        .antMatchers(HttpMethod.DELETE, "/v1/resignation/settings/resignationAssignments/{resignationTemplateId}").hasAnyRole("assignResignationTempAdmin", "admin")
                        .antMatchers(HttpMethod.POST, "/v1/document/upload/**").hasAnyRole("user", "admin", "restrictAdmin")

                        //END FOR resignation OF ADMIN

                        //RULE FOR RESTRICT ADMIN ROLE ENDS HERE
                        // for rest of the api paths, you need to have admin role in token
                        .antMatchers("/v1/**").hasRole("admin")

                        .anyRequest().permitAll();

            }

        }
    }
}
