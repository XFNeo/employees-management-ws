package ru.xfneo.employeesmanagement.controller;

import feign.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import ru.xfneo.employeesmanagement.client.DepartmentClient;
import ru.xfneo.employeesmanagement.model.Employee;
import ru.xfneo.employeesmanagement.repository.EmployeeRepository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeesControllerIT {
    @MockBean
    private DepartmentClient departmentClient;
    @Mock
    Response mockResponse;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DataSource dataSource;
    private Employee employee1;
    private Employee employee2;
    private Employee employee3;
    private Employee employee4;
    private static final String EMPLOYEES_URI = "/api/employees";
    private static final String EMPLOYEES_ID_URI = "/api/employees/{id}";
    private static final String EMPLOYEES_REPLACE_DEPARTMENT_URI = "/api/employees/replaceDepartment";


    @Before
    public void setUp() {
        employee1 = new Employee(1, 1, "Maksim", "Tikhonov", "Lead IT Engineer");
        employee2 = new Employee(2, 1, "Sergey", "Ivanov", "Senior IT Engineer");
        employee3 = new Employee(3, 2, "Anna", "Demeleva", "QA Engineer");
        employee4 = new Employee(4, 3, "Svetlana", "Melnikova", "Accounting specialist");
        employeeRepository.save(employee1);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
        employeeRepository.save(employee4);
    }

    @After
    public void resetDb() {
        employeeRepository.deleteAll();
        new JdbcTemplate(dataSource).update("ALTER SEQUENCE hibernate_sequence RESTART WITH 1");
    }

    @Test
    public void getListOfEmployeesAndNotEmptyListOfEmployees() {
        ResponseEntity<List<Employee>> response = restTemplate.exchange(EMPLOYEES_URI, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Employee>>() {
                });
        List<Employee> employees = response.getBody();
        assertThat(employees, hasSize(4));
        assertThat(employees.get(0).getFirstName(), is("Maksim"));
    }

    @Test
    public void getEmployeeAndNotNullEmployee() {
        long id = employee2.getId();
        Employee actualEmployee = restTemplate.getForObject(EMPLOYEES_ID_URI, Employee.class, id);
        assertEquals(employee2, actualEmployee);
    }

    @Test
    public void createEmployeeAndSavedEmployeeInRepository() {
        Employee expectedEmployee = new Employee(5, 2, "Maria", "Grenkova", "Senior accounting specialist");
        when(mockResponse.status()).thenReturn(200);
        when(departmentClient.checkDepartment(expectedEmployee.getDepartmentId())).thenReturn(mockResponse);
        ResponseEntity<Employee> response = restTemplate.postForEntity(EMPLOYEES_URI, expectedEmployee, Employee.class);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(response.getBody().getId(), notNullValue());
        assertThat(response.getBody().getFirstName(), is(expectedEmployee.getFirstName()));
        Employee actualEmployee = restTemplate.getForObject(EMPLOYEES_ID_URI, Employee.class, expectedEmployee.getId());
        assertEquals(expectedEmployee, actualEmployee);
    }

    @Test
    public void updateEmployeeAndUpdatedEmployeeInRepository() {
        Employee expectedEmployee = new Employee(999, 2, "Anna", "Demeleva", "Senior QA Engineer");
        HttpEntity<Employee> entity = new HttpEntity<>(expectedEmployee);
        when(mockResponse.status()).thenReturn(200);
        when(departmentClient.checkDepartment(expectedEmployee.getDepartmentId())).thenReturn(mockResponse);
        ResponseEntity<Employee> response =
                restTemplate.exchange(EMPLOYEES_ID_URI, HttpMethod.PUT, entity, Employee.class, employee3.getId());
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getId(), is(employee3.getId()));
        assertThat(response.getBody().getFirstName(), is(employee3.getFirstName()));
        assertThat(response.getBody().getTitle(), is(expectedEmployee.getTitle()));
        Employee actualEmployee = restTemplate.getForObject(EMPLOYEES_ID_URI, Employee.class, employee3.getId());
        assertEquals(actualEmployee.getTitle(), expectedEmployee.getTitle());
    }

    @Test
    public void replaceDepartmentIdAndUpdatedEmployeesWithNewDepartment() {
        Map<String, Long> departments = new HashMap<>();
        departments.put("oldDepartmentID", employee1.getDepartmentId());
        departments.put("newDepartmentID", employee4.getDepartmentId());
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(departments);
        when(mockResponse.status()).thenReturn(200);
        when(departmentClient.checkDepartment(employee4.getDepartmentId())).thenReturn(mockResponse);
        ResponseEntity<Object> response =
                restTemplate.exchange(EMPLOYEES_REPLACE_DEPARTMENT_URI, HttpMethod.POST, entity, Object.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        ResponseEntity<List<Employee>> getResponse = restTemplate.exchange(EMPLOYEES_URI, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Employee>>() {
                });
        List<Employee> employees = getResponse.getBody();
        assertThat(employees.get(0).getDepartmentId(), is(employee4.getDepartmentId()));
        assertThat(employees.get(1).getDepartmentId(), is(employee4.getDepartmentId()));
        assertThat(employees.get(2).getDepartmentId(), is(employee3.getDepartmentId()));
        assertThat(employees.get(3).getDepartmentId(), is(employee4.getDepartmentId()));
    }

    @Test
    public void deleteEmployeeAndDeletedEmployeeFromRepository() {
        long id = employee4.getId();
        restTemplate.delete(EMPLOYEES_ID_URI, id);
        ResponseEntity<Void> response =
                restTemplate.exchange(EMPLOYEES_ID_URI, HttpMethod.GET, null, Void.class, id);
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }
}