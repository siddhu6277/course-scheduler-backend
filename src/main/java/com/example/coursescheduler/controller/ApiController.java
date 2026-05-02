package com.example.coursescheduler.controller;

import com.example.coursescheduler.model.*;
import com.example.coursescheduler.service.JsonDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private final JsonDataService dataService;

    public ApiController(JsonDataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "Backend running on port 3001");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        List<User> users = dataService.getUsers();
        Optional<User> user = users.stream()
                .filter(u -> username != null && username.equals(u.username) && password != null && password.equals(u.password))
                .findFirst();
        if (user.isPresent()) {
            User u = user.get();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userWithoutPassword(u));
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String email = (String) body.get("email");
        String name = (String) body.get("name");
        String role = (String) body.getOrDefault("role", "student");

        if (username == null || password == null || email == null || name == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "All fields are required"));
        }

        List<User> users = dataService.getUsers();
        if (users.stream().anyMatch(u -> u.username.equals(username))) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", "Username already exists"));
        }
        if (users.stream().anyMatch(u -> u.email.equals(email))) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", "Email already registered"));
        }

        User newUser = new User(dataService.nextId(), username, password, name, email, role);
        users.add(newUser);
        dataService.saveUsers(users);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", userWithoutPassword(newUser));
        response.put("message", "Account created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/courses")
    public List<Course> getCourses() {
        return dataService.getCourses();
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<?> getCourse(@PathVariable String id) {
        return dataService.getCourses().stream()
                .filter(c -> c.id.equals(id))
                .findFirst()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Course not found")));
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> addCourse(@RequestBody Course course) {
        List<Course> courses = dataService.getCourses();
        course.id = "COURSE" + System.currentTimeMillis();
        course.enrolled = 0;
        courses.add(course);
        dataService.saveCourses(courses);
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable String id, @RequestBody Course course) {
        List<Course> courses = dataService.getCourses();
        Optional<Course> existing = courses.stream().filter(c -> c.id.equals(id)).findFirst();
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Course not found"));
        }
        Course current = existing.get();
        current.name = course.name;
        current.description = course.description;
        current.instructor = course.instructor;
        current.credits = course.credits;
        current.days = course.days;
        current.startTime = course.startTime;
        current.endTime = course.endTime;
        current.location = course.location;
        current.capacity = course.capacity;
        current.semester = course.semester;
        dataService.saveCourses(courses);
        return ResponseEntity.ok(current);
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable String id) {
        List<Course> courses = dataService.getCourses();
        List<Course> filtered = courses.stream().filter(c -> !c.id.equals(id)).toList();
        if (filtered.size() == courses.size()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Course not found"));
        }
        dataService.saveCourses(filtered);
        return ResponseEntity.ok(Map.of("message", "Course deleted"));
    }

    @GetMapping("/registrations/{studentId}")
    public List<Registration> getRegistrations(@PathVariable String studentId) {
        return dataService.getRegistrations().stream()
                .filter(r -> studentId.equals(r.studentId))
                .toList();
    }

    @PostMapping("/registrations")
    public ResponseEntity<?> registerCourse(@RequestBody Map<String, Object> body) {
        String studentId = (String) body.get("studentId");
        String courseId = (String) body.get("courseId");

        List<Registration> registrations = dataService.getRegistrations();
        List<Course> courses = dataService.getCourses();
        Course course = courses.stream().filter(c -> c.id.equals(courseId)).findFirst().orElse(null);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Course not found"));
        }
        if (registrations.stream().anyMatch(r -> studentId.equals(r.studentId) && courseId.equals(r.courseId))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Already registered for this course"));
        }
        if (course.enrolled >= course.capacity) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Course is full"));
        }
        List<String> studentCourses = registrations.stream()
                .filter(r -> studentId.equals(r.studentId))
                .map(r -> r.courseId)
                .toList();
        for (String otherId : studentCourses) {
            Course otherCourse = courses.stream().filter(c -> c.id.equals(otherId)).findFirst().orElse(null);
            if (otherCourse != null && timeConflict(otherCourse.days, otherCourse.startTime, otherCourse.endTime, course.days, course.startTime, course.endTime)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Schedule conflict detected"));
            }
        }

        Registration registration = new Registration(dataService.nextId(), studentId, courseId, dataService.now(), "active");
        registrations.add(registration);
        course.enrolled++;
        dataService.saveRegistrations(registrations);
        dataService.saveCourses(courses);
        return ResponseEntity.status(HttpStatus.CREATED).body(registration);
    }

    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<?> dropCourse(@PathVariable String id) {
        List<Registration> registrations = dataService.getRegistrations();
        Optional<Registration> registration = registrations.stream().filter(r -> r.id.equals(id)).findFirst();
        if (registration.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Registration not found"));
        }
        List<Course> courses = dataService.getCourses();
        courses.stream().filter(c -> c.id.equals(registration.get().courseId)).findFirst().ifPresent(c -> {
            if (c.enrolled > 0) c.enrolled--;
        });
        dataService.saveCourses(courses);
        dataService.saveRegistrations(registrations.stream().filter(r -> !r.id.equals(id)).toList());
        return ResponseEntity.ok(Map.of("message", "Course dropped successfully"));
    }

    @GetMapping("/dashboard/{userId}")
    public Map<String, Object> dashboard(@PathVariable String userId) {
        List<Registration> registrations = dataService.getRegistrations();
        List<Course> courses = dataService.getCourses();
        List<Grade> grades = dataService.getGrades();

        List<Registration> userRegs = registrations.stream().filter(r -> userId.equals(r.studentId)).toList();
        List<Course> enrolledCourses = courses.stream()
                .filter(c -> userRegs.stream().map(r -> r.courseId).anyMatch(cid -> cid.equals(c.id)))
                .toList();
        List<Grade> userGrades = grades.stream().filter(g -> userId.equals(g.studentId)).toList();

        return Map.of(
                "totalEnrolled", userRegs.size(),
                "totalCredits", enrolledCourses.stream().mapToInt(c -> c.credits).sum(),
                "gpa", calculateGpa(userGrades),
                "completedCourses", userGrades.size(),
                "enrolledCourses", enrolledCourses
        );
    }

    @PostMapping("/grades")
    public ResponseEntity<?> addGrade(@RequestBody Grade grade) {
        List<Grade> grades = dataService.getGrades();
        int existing = -1;
        for (int i = 0; i < grades.size(); i++) {
            Grade g = grades.get(i);
            if (grade.studentId.equals(g.studentId) && grade.courseId.equals(g.courseId)) {
                existing = i;
                break;
            }
        }
        grade.date = dataService.now();
        if (existing != -1) {
            grades.set(existing, grade);
        } else {
            grades.add(grade);
        }
        dataService.saveGrades(grades);
        return ResponseEntity.ok(grade);
    }

    @GetMapping("/grades/{studentId}")
    public Map<String, Object> getGrades(@PathVariable String studentId) {
        List<Grade> studentGrades = dataService.getGrades().stream()
                .filter(g -> studentId.equals(g.studentId))
                .toList();
        return Map.of("grades", studentGrades, "gpa", calculateGpa(studentGrades));
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> addReview(@RequestBody Review review) {
        review.id = dataService.nextId();
        review.date = dataService.now();
        List<Review> reviews = dataService.getReviews();
        reviews.add(review);
        dataService.saveReviews(reviews);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/reviews/{courseId}")
    public Map<String, Object> getReviews(@PathVariable String courseId) {
        List<Review> courseReviews = dataService.getReviews().stream()
                .filter(r -> courseId.equals(r.courseId))
                .toList();
        double avgRating = courseReviews.isEmpty() ? 0 : courseReviews.stream().mapToDouble(r -> r.rating).average().orElse(0);
        return Map.of("reviews", courseReviews, "avgRating", String.format("%.1f", avgRating), "totalReviews", courseReviews.size());
    }

    @PostMapping("/favorites")
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, Object> body) {
        String studentId = (String) body.get("studentId");
        String courseId = (String) body.get("courseId");
        List<Favorite> favorites = dataService.getFavorites();
        if (favorites.stream().anyMatch(f -> studentId.equals(f.studentId) && courseId.equals(f.courseId))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Already in favorites"));
        }
        Favorite favorite = new Favorite(dataService.nextId(), studentId, courseId, dataService.now());
        favorites.add(favorite);
        dataService.saveFavorites(favorites);
        return ResponseEntity.ok(favorite);
    }

    @DeleteMapping("/favorites/{studentId}/{courseId}")
    public ResponseEntity<?> removeFavorite(@PathVariable String studentId, @PathVariable String courseId) {
        List<Favorite> favorites = dataService.getFavorites();
        List<Favorite> filtered = favorites.stream()
                .filter(f -> !(studentId.equals(f.studentId) && courseId.equals(f.courseId)))
                .toList();
        dataService.saveFavorites(filtered);
        return ResponseEntity.ok(Map.of("message", "Removed from favorites"));
    }

    @GetMapping("/favorites/{studentId}")
    public List<Favorite> getFavorites(@PathVariable String studentId) {
        return dataService.getFavorites().stream()
                .filter(f -> studentId.equals(f.studentId))
                .toList();
    }

    @PostMapping("/waitlist")
    public ResponseEntity<?> addWaitlistEntry(@RequestBody Map<String, Object> body) {
        String studentId = (String) body.get("studentId");
        String courseId = (String) body.get("courseId");
        List<WaitlistEntry> waitlist = dataService.getWaitlist();
        if (waitlist.stream().anyMatch(w -> studentId.equals(w.studentId) && courseId.equals(w.courseId))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Already on waitlist"));
        }
        WaitlistEntry entry = new WaitlistEntry(dataService.nextId(), studentId, courseId, waitlist.size() + 1, dataService.now());
        waitlist.add(entry);
        dataService.saveWaitlist(waitlist);
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/waitlist/{courseId}")
    public List<WaitlistEntry> getWaitlistByCourse(@PathVariable String courseId) {
        return dataService.getWaitlist().stream()
                .filter(w -> courseId.equals(w.courseId))
                .toList();
    }

    @GetMapping("/waitlist/student/{studentId}")
    public List<WaitlistEntry> getWaitlistByStudent(@PathVariable String studentId) {
        return dataService.getWaitlist().stream()
                .filter(w -> studentId.equals(w.studentId))
                .toList();
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable String userId) {
        return dataService.getUsers().stream()
                .filter(u -> u.id.equals(userId))
                .findFirst()
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(userWithoutPassword(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found")));
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable String userId, @RequestBody Map<String, Object> updates) {
        List<User> users = dataService.getUsers();
        Optional<User> optionalUser = users.stream().filter(u -> u.id.equals(userId)).findFirst();
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
        User user = optionalUser.get();
        if (updates.containsKey("username")) user.username = (String) updates.get("username");
        if (updates.containsKey("password")) user.password = (String) updates.get("password");
        if (updates.containsKey("email")) user.email = (String) updates.get("email");
        if (updates.containsKey("name")) user.name = (String) updates.get("name");
        if (updates.containsKey("role")) user.role = (String) updates.get("role");
        dataService.saveUsers(users);
        return ResponseEntity.ok(userWithoutPassword(user));
    }

    @GetMapping("/analytics")
    public Map<String, Object> analytics() {
        List<Course> courses = dataService.getCourses();
        List<Registration> registrations = dataService.getRegistrations();
        List<User> users = dataService.getUsers();

        long totalStudents = users.stream().filter(u -> "student".equals(u.role)).count();
        long totalCourses = courses.size();
        long totalEnrollments = registrations.size();
        double avgCapacity = totalCourses > 0 ? courses.stream().mapToInt(c -> c.enrolled).average().orElse(0) : 0;

        List<Map<String, Object>> courseStats = courses.stream()
                .map(c -> Map.<String, Object>of(
                        "id", c.id,
                        "name", c.name,
                        "enrolled", c.enrolled,
                        "capacity", c.capacity,
                        "utilization", String.format("%.1f", c.capacity > 0 ? (c.enrolled / (double) c.capacity) * 100 : 0)
                ))
                .collect(Collectors.toList());

        return Map.of(
                "totalStudents", totalStudents,
                "totalCourses", totalCourses,
                "totalEnrollments", totalEnrollments,
                "avgCapacity", String.format("%.1f", avgCapacity),
                "fullCourses", courses.stream().filter(c -> c.enrolled >= c.capacity).count(),
                "courseStats", courseStats
        );
    }

    @GetMapping("/search")
    public List<Course> searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer credits,
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String time
    ) {
        List<Course> courses = new ArrayList<>(dataService.getCourses());
        if (q != null && !q.isBlank()) {
            String query = q.toLowerCase();
            courses = courses.stream()
                    .filter(c -> c.name.toLowerCase().contains(query) || c.instructor.toLowerCase().contains(query))
                    .toList();
        }
        if (credits != null) {
            courses = courses.stream().filter(c -> c.credits == credits).toList();
        }
        if (day != null && !day.isBlank()) {
            courses = courses.stream().filter(c -> c.days.contains(day)).toList();
        }
        if (time != null && !time.isBlank()) {
            courses = courses.stream().filter(c -> c.startTime != null && c.startTime.contains(time)).toList();
        }
        return courses;
    }

    private Map<String, Object> userWithoutPassword(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.id);
        result.put("username", user.username);
        result.put("name", user.name);
        result.put("email", user.email);
        result.put("role", user.role);
        return result;
    }

    private double calculateGpa(List<Grade> grades) {
        Map<String, Double> gradePoints = Map.of(
                "A", 4.0,
                "A-", 3.7,
                "B+", 3.3,
                "B", 3.0,
                "B-", 2.7,
                "C+", 2.3,
                "C", 2.0,
                "C-", 1.7,
                "D", 1.0,
                "F", 0.0
        );
        if (grades.isEmpty()) {
            return 0.0;
        }
        double total = grades.stream()
                .mapToDouble(g -> gradePoints.getOrDefault(g.grade, 0.0))
                .sum();
        return Math.round((total / grades.size()) * 100.0) / 100.0;
    }

    private boolean timeConflict(List<String> days1, String start1, String end1, List<String> days2, String start2, String end2) {
        if (days1 == null || days2 == null) {
            return false;
        }
        boolean sameDays = days1.stream().anyMatch(days2::contains);
        if (!sameDays) {
            return false;
        }
        int s1 = parseTime(start1);
        int e1 = parseTime(end1);
        int s2 = parseTime(start2);
        int e2 = parseTime(end2);
        return s1 < e2 && s2 < e1;
    }

    private int parseTime(String time) {
        if (time == null || !time.contains(":")) {
            return 0;
        }
        String[] parts = time.split(":");
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return hour * 60 + minute;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
