package com.example.CSVDemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.CSVDemo.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {


}
