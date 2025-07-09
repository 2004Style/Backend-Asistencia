package com.systems.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.systems.dto.AttendanceReportDTO;
import com.systems.model.Attendance;

public interface IAttendanceRepo extends IGenericRepo<Attendance, Integer> {
    List<Attendance> findByClassroom_IdClassroom(Integer classroomId);

    @Query(value = "SELECT new com.systems.dto.AttendanceReportDTO(s.idStudent, p.firstName, p.lastName, " +
            "SUM(CASE WHEN a.isPresent = true AND a.isLate = false THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN a.isLate = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN a.isPresent = false THEN 1 ELSE 0 END)) " +
            "FROM Attendance a " +
            "JOIN a.student s " +
            "JOIN s.person p " +
            "WHERE a.classroom.idClassroom = :classroomId " +
            "AND a.date BETWEEN :startDate AND :endDate " +
            "GROUP BY s.idStudent, p.firstName, p.lastName")
    List<AttendanceReportDTO> getAttendanceReport(@Param("classroomId") Integer classroomId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT new map(" +
            "SUM(CASE WHEN a.isPresent = true AND a.isLate = false THEN 1 ELSE 0 END) as PRESENT, " +
            "SUM(CASE WHEN a.isLate = true THEN 1 ELSE 0 END) as LATE, " +
            "SUM(CASE WHEN a.isPresent = false THEN 1 ELSE 0 END) as ABSENT) " +
            "FROM Attendance a " +
            "WHERE a.student.idStudent = :studentId AND a.classroom.idClassroom = :classroomId")
    Map<String, Long> getStudentAttendanceStatsInClassroom(@Param("studentId") Integer studentId,
            @Param("classroomId") Integer classroomId);
}
