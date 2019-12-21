package ru.xfneo.employeesmanagement.service;

import feign.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import ru.xfneo.employeesmanagement.client.DepartmentClient;
import ru.xfneo.employeesmanagement.model.DepartmentsToReplaceDto;
import ru.xfneo.employeesmanagement.model.Employee;
import ru.xfneo.employeesmanagement.repository.EmployeeRepository;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DepartmentClient departmentClient;
    @Mock
    Response mockResponse;
    @InjectMocks
    private EmployeeService sut;
    private Employee employee1, employee2;

    @Before
    public void setUp() {
        employee1 = new Employee(1, 1, "firstName1", "lastName1", "title1");
        employee2 = new Employee(2, 2, "firstName2", "lastName2", "title2");
    }

    @Test
    public void findAll_GetAllEmployees_NotEmptyListOfEmployees() {
        List<Employee> expectedList = Arrays.asList(employee1, employee2);
        when(employeeRepository.findAll()).thenReturn(expectedList);
        List<Employee> actualList = sut.findAll();
        verify(employeeRepository).findAll();
        assertEquals(expectedList, actualList);
        verifyNoMoreInteractions(employeeRepository);
    }

    @Test
    public void find_GetEmployee_OkResponseWithEmployee() {
        when(employeeRepository.findById(employee1.getId())).thenReturn(Optional.of(employee1));
        ResponseEntity<?> expectedResponse = ResponseEntity.ok(employee1);
        ResponseEntity<?> actualResponse = sut.find(employee1.getId());
        assertEquals(expectedResponse, actualResponse);
        verify(employeeRepository).findById(employee1.getId());
    }

    @Test
    public void find_GetNullEmployee_NotFoundResponse() {
        ResponseEntity<?> expectedNotFoundResponse = ResponseEntity.status(404).body("Employee Not Found");
        ResponseEntity<?> actualFailResponse = sut.find(null);
        assertEquals(expectedNotFoundResponse, actualFailResponse);
    }

    @Test
    public void create_CreateEmployee_CreatedResponseWithEmployee() {
        when(mockResponse.status()).thenReturn(200);
        when(departmentClient.checkDepartment(employee1.getDepartmentId())).thenReturn(mockResponse);
        when(employeeRepository.save(employee1)).thenReturn(employee1);
        ResponseEntity<?> expectedResponse = ResponseEntity.status(201).body(employee1);
        ResponseEntity<?> actualResponse = sut.create(employee1);
        assertEquals(expectedResponse, actualResponse);
        verify(employeeRepository).save(employee1);
    }

    @Test
    public void create_CreateEmployeeWithWrongDepartment_NotFoundResponse() {
        when(mockResponse.status()).thenReturn(400);
        when(departmentClient.checkDepartment(employee1.getDepartmentId())).thenReturn(mockResponse);
        ResponseEntity<?> expectedResponse = ResponseEntity.status(400).body("Department with id " + employee1.getDepartmentId() + " not found!");
        ResponseEntity<?> actualResponse = sut.create(employee1);
        assertEquals(expectedResponse, actualResponse);
        verify(employeeRepository, times(0)).save(any(Employee.class));
    }

    @Test
    public void update_UpdateEmployee_OkResponseWithUpdatedEmployee() {
        when(mockResponse.status()).thenReturn(200);
        when(departmentClient.checkDepartment(employee2.getDepartmentId())).thenReturn(mockResponse);
        when(employeeRepository.save(employee1)).thenReturn(employee1);
        when(employeeRepository.findById(employee1.getId())).thenReturn(Optional.of(employee1));
        ResponseEntity<?> expectedResponse = ResponseEntity.ok(employee1);
        ResponseEntity<?> actualResponse = sut.update(employee1.getId(), employee2);
        assertEquals(expectedResponse, actualResponse);
        assertEquals(employee1.getId(), ((Employee) Objects.requireNonNull(actualResponse.getBody())).getId());
        assertEquals(employee2.getDepartmentId(), ((Employee) Objects.requireNonNull(actualResponse.getBody())).getDepartmentId());
        assertEquals(employee2.getFirstName(), ((Employee) Objects.requireNonNull(actualResponse.getBody())).getFirstName());
        assertEquals(employee2.getLastName(), ((Employee) Objects.requireNonNull(actualResponse.getBody())).getLastName());
        assertEquals(employee2.getTitle(), ((Employee) Objects.requireNonNull(actualResponse.getBody())).getTitle());
        verify(employeeRepository ).save(employee1);
        verify(employeeRepository).findById(employee1.getId());
    }

    @Test
    public void update_UpdateEmployeeWithWrongDepartment_NotFoundResponse() {
        when(mockResponse.status()).thenReturn(400);
        when(departmentClient.checkDepartment(employee2.getDepartmentId())).thenReturn(mockResponse);
        when(employeeRepository.findById(employee1.getId())).thenReturn(Optional.of(employee1));
        long employee1OriginId = employee1.getId();
        long employee1OriginDepartmentId = employee1.getDepartmentId();
        ResponseEntity<?> expectedResponse = ResponseEntity.status(400).body("Department with id " + employee2.getDepartmentId() + " not found!");
        ResponseEntity<?> actualResponse = sut.update(employee1.getId(), employee2);
        assertEquals(expectedResponse, actualResponse);
        assertEquals(employee1OriginId, employee1.getId());
        assertEquals(employee1OriginDepartmentId, employee1.getDepartmentId());
        verify(employeeRepository, times(0)).save(any(Employee.class));
        verify(employeeRepository).findById(employee1.getId());
    }

    @Test
    public void update_UpdateNonexistentEmployee_NotFoundResponse() {
        when(employeeRepository.findById(employee1.getId())).thenReturn(Optional.empty());
        ResponseEntity<?> expectedResponse = ResponseEntity.status(404).body("Employee Not Found");
        ResponseEntity<?> actualResponse = sut.update(employee1.getId(), employee2);
        assertEquals(expectedResponse, actualResponse);
        verify(employeeRepository, times(0)).save(any(Employee.class));
        verify(employeeRepository).findById(employee1.getId());
    }

    @Test
    public void delete_DeleteEmployee_NoContentResponse() {
        when(employeeRepository.findById(employee1.getId())).thenReturn(Optional.of(employee1));
        ResponseEntity<?> expectedResponse = ResponseEntity.status(204).build();
        ResponseEntity<?> actualResponse = sut.delete(employee1.getId());
        assertEquals(expectedResponse, actualResponse);
        verify(employeeRepository).deleteById(employee1.getId());
    }

    @Test
    public void delete_DeleteNullEmployee_NoContentResponse() {
        ResponseEntity<?> expectedResponse = ResponseEntity.status(204).build();
        ResponseEntity<?> actualResponse = sut.delete(null);
        assertEquals(expectedResponse, actualResponse);
        verify(employeeRepository, times(0)).delete(any(Employee.class));
    }

    @Test
    public void replaceDepartmentId_ReplaceDepartments_OkResponse() {
        DepartmentsToReplaceDto departments = new DepartmentsToReplaceDto();
        departments.setOldDepartmentID(employee1.getDepartmentId());
        departments.setNewDepartmentID(employee2.getDepartmentId());
        long employee1OriginDepartmentId = employee1.getDepartmentId();
        when(employeeRepository.findByDepartmentId(employee1.getDepartmentId())).thenReturn(Collections.singletonList(employee1));
        when(mockResponse.status()).thenReturn(200);
        when(departmentClient.checkDepartment(employee2.getDepartmentId())).thenReturn(mockResponse);
        ResponseEntity<?> expectedResponse = ResponseEntity.ok("1 employee(s) are affected");
        ResponseEntity<?> actualResponse = sut.replaceDepartmentId(departments);
        assertEquals(expectedResponse, actualResponse);
        assertEquals(employee2.getDepartmentId(), employee1.getDepartmentId());
        verify(employeeRepository).save(employee1);
        verify(employeeRepository).findByDepartmentId(employee1OriginDepartmentId);
        verify(departmentClient).checkDepartment(employee2.getDepartmentId());
    }

    @Test
    public void replaceDepartmentId_ReplaceDepartmentOnNonexistentDepartment_NotFoundResponse() {
        DepartmentsToReplaceDto departments = new DepartmentsToReplaceDto();
        departments.setOldDepartmentID(employee1.getDepartmentId());
        departments.setNewDepartmentID(employee2.getDepartmentId());
        long employee1OriginDepartmentId = employee1.getDepartmentId();
        when(employeeRepository.findByDepartmentId(employee1.getDepartmentId())).thenReturn(Collections.singletonList(employee1));
        when(mockResponse.status()).thenReturn(400);
        when(departmentClient.checkDepartment(employee2.getDepartmentId())).thenReturn(mockResponse);
        ResponseEntity<?> expectedResponse = ResponseEntity.status(400).body("Department with id "+ employee2.getDepartmentId() + " not found!");
        ResponseEntity<?> actualResponse = sut.replaceDepartmentId(departments);
        assertEquals(expectedResponse, actualResponse);
        assertEquals(employee1OriginDepartmentId, employee1.getDepartmentId());
        verify(employeeRepository, times(0)).save(any(Employee.class));
        verify(employeeRepository).findByDepartmentId(employee1.getDepartmentId());
        verify(departmentClient).checkDepartment(employee2.getDepartmentId());
    }
}