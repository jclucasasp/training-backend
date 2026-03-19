//package org.lucas.arbackend;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//import java.util.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import jakarta.persistence.EntityNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.lucas.arbackend.dto.course.CourseChapterRequest;
//import org.lucas.arbackend.dto.course.CourseRequest;
//import org.lucas.arbackend.dto.course.CourseResponse;
//import org.lucas.arbackend.entity.Organisation.Organisation;
//import org.lucas.arbackend.entity.Organisation.Staff;
//import org.lucas.arbackend.entity.course.Chapter;
//import org.lucas.arbackend.entity.course.Course;
//import org.lucas.arbackend.entity.quiz.Quiz;
//import org.lucas.arbackend.mapper.CourseMapper;
//import org.lucas.arbackend.repository.course.ChapterRepository;
//import org.lucas.arbackend.repository.course.CourseRepository;
//import org.lucas.arbackend.repository.organisation.OrganisationRepository;
//import org.lucas.arbackend.repository.organisation.StaffRepository;
//import org.lucas.arbackend.repository.quiz.QuizRepository;
//import org.lucas.arbackend.service.course.CourseService;
//import org.lucas.arbackend.util.tenant.TenantProvider;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//
//@ExtendWith(MockitoExtension.class)
//class CourseServiceTest {
//
//    @Mock
//    private CourseRepository courseRepo;
//
//    @Mock
//    private OrganisationRepository orgRepo;
//
//    @Mock
//    private TenantProvider tenantProvider;
//
//    @Mock
//    private CourseMapper courseMapper;
//
//    @Mock
//    private StaffRepository staffRepo;
//
//    @Mock
//    private QuizRepository quizRepo;
//
//    @Mock
//    private ChapterRepository chapterRepo;
//
//    @InjectMocks
//    private CourseService courseService;
//
//    private Organisation organisation;
//    private Staff staff;
//    private CourseRequest courseRequest;
//    private Course course;
//    private CourseResponse courseResponse;
//
//    @BeforeEach
//    void setUp() {
//        organisation = new Organisation();
//        organisation.setId(1L);
//
//        staff = new Staff();
//        staff.setId(1L);
//        staff.setEmail("staff@example.com");
//
//        courseRequest = new CourseRequest();
//        courseRequest.setStaffEmail("staff@example.com");
//
//        course = new Course();
//        course.setId(1L);
//        course.setStaff(staff);
//
//        courseResponse = new CourseResponse(1L);
//        courseResponse.setId(1L);
//
//        // Default tenant behavior
//        when(tenantProvider.get()).thenReturn(1L);
//        when(orgRepo.findById(anyLong())).thenReturn(Optional.of(organisation));
//    }
//
//    @Test
//    void createCourse_Success() {
//        // Arrange
//        CourseChapterRequest chapterRequest = new CourseChapterRequest();
//        chapterRequest.setSections(new ArrayList<>());
//        courseRequest.setChapters(new ArrayList<>());
//        courseRequest.getChapters().add(chapterRequest);
//
//        when(staffRepo.findByEmailAndOrganisationIdAndEndedAtIsNull(anyString(), anyLong()))
//                .thenReturn(Optional.of(staff));
//        when(courseRepo.save(any(Course.class))).thenReturn(course);
//        when(courseMapper.maptoCourseResponse(any(Course.class))).thenReturn(courseResponse);
//
//        // Act
//        CourseResponse result = courseService.createCourse(courseRequest);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(courseResponse.id(), result.id());
//        verify(courseRepo, times(1)).save(any(Course.class));
//    }
//
//    @Test
//    void createCourse_StaffNotFound_ThrowsException() {
//        // Arrange
//        when(staffRepo.findByEmailAndOrganisationIdAndEndedAtIsNull(anyString(), anyLong()))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(EntityNotFoundException.class, () -> courseService.createCourse(courseRequest));
//    }
//
//    @Test
//    void getPaginatedCourses_Success() {
//        // Arrange
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Course> coursePage = new PageImpl<>(Collections.singletonList(course));
//
//        when(courseRepo.findAllByOrganisationIdAndEndedAtIsNull(anyLong(), any(Pageable.class)))
//                .thenReturn(coursePage);
//        when(courseMapper.maptoCourseResponse(any(Course.class))).thenReturn(courseResponse);
//
//        // Act
//        Page<CourseResponse> result = courseService.getPaginatedCourses(pageable);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.getContent().size());
//        assertEquals(courseResponse.id(), result.getContent().get(0).id());
//    }
//
//    @Test
//    void updateCourse_Success() {
//        // Arrange
//        CourseChapterRequest chapterRequest = new CourseChapterRequest();
//        chapterRequest.setId(1L);
//        chapterRequest.setSections(new ArrayList<>());
//        courseRequest.setChapters(new ArrayList<>());
//        courseRequest.getChapters().add(chapterRequest);
//
//        Chapter chapter = new Chapter();
//        chapter.setId(1L);
//        course.setChapters(new HashSet<>(Collections.singletonList(chapter)));
//
//        when(courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(anyLong(), anyLong()))
//                .thenReturn(Optional.of(course));
//        when(courseRepo.save(any(Course.class))).thenReturn(course);
//        when(courseMapper.maptoCourseResponse(any(Course.class))).thenReturn(courseResponse);
//
//        // Act
//        CourseResponse result = courseService.updateCourse(1L, courseRequest);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(courseResponse.getId(), result.getId());
//        verify(courseRepo, times(1)).save(any(Course.class));
//    }
//
//    @Test
//    void updateCourse_CourseNotFound_ThrowsException() {
//        // Arrange
//        when(courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(anyLong(), anyLong()))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(EntityNotFoundException.class, () -> courseService.updateCourse(1L, courseRequest));
//    }
//
//    @Test
//    void softDeleteCourse_Success() {
//        // Arrange
//        when(courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(anyLong(), anyLong()))
//                .thenReturn(Optional.of(course));
//
//        // Act
//        courseService.softDeleteCourse(1L);
//
//        // Assert
//        verify(courseRepo, times(1)).delete(any(Course.class));
//    }
//
//    @Test
//    void softDeleteCourse_CourseNotFound_ThrowsException() {
//        // Arrange
//        when(courseRepo.findByIdAndOrganisationIdAndEndedAtIsNull(anyLong(), anyLong()))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(EntityNotFoundException.class, () -> courseService.softDeleteCourse(1L));
//    }
//
//    @Test
//    void addQuizToChapter_Success() {
//        // Arrange
//        Chapter chapter = new Chapter();
//        chapter.setId(1L);
//
//        Quiz quiz = new Quiz();
//        quiz.setId(1L);
//
//        when(chapterRepo.findByIdAndOrganisationId(anyLong(), anyLong()))
//                .thenReturn(Optional.of(chapter));
//        when(quizRepo.findByIdAndOrganisationIdAndEndedAtIsNull(anyLong(), anyLong()))
//                .thenReturn(Optional.of(quiz));
//
//        // Act
//        courseService.addQuizToChapter(1L, 1L);
//
//        // Assert
//        assertTrue(chapter.getQuizzes().contains(quiz));
//        verify(chapterRepo, times(1)).save(any(Chapter.class));
//    }
//
//    @Test
//    void addQuizToChapter_ChapterNotFound_ThrowsException() {
//        // Arrange
//        when(chapterRepo.findByIdAndOrganisationId(anyLong(), anyLong()))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(EntityNotFoundException.class, () -> courseService.addQuizToChapter(1L, 1L));
//    }
//
//    @Test
//    void addQuizToChapter_QuizNotFound_ThrowsException() {
//        // Arrange
//        Chapter chapter = new Chapter();
//        chapter.setId(1L);
//
//        when(chapterRepo.findByIdAndOrganisationId(anyLong(), anyLong()))
//                .thenReturn(Optional.of(chapter));
//        when(quizRepo.findByIdAndOrganisationIdAndEndedAtIsNull(anyLong(), anyLong()))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThrows(EntityNotFoundException.class, () -> courseService.addQuizToChapter(1L, 1L));
//    }
//
//    @Test
//    void isOwner_Success() {
//        // Arrange
//        when(courseRepo.findById(anyLong())).thenReturn(Optional.of(course));
//
//        // Act
//        boolean result = courseService.isOwner(1L, 1L);
//
//        // Assert
//        assertTrue(result);
//    }
//
//    @Test
//    void isOwner_NotOwner_ReturnsFalse() {
//        // Arrange
//        Staff otherStaff = new Staff();
//        otherStaff.setId(2L);
//        course.setStaff(otherStaff);
//
//        when(courseRepo.findById(anyLong())).thenReturn(Optional.of(course));
//
//        // Act
//        boolean result = courseService.isOwner(1L, 1L);
//
//        // Assert
//        assertFalse(result);
//    }
//
//    @Test
//    void isOwner_CourseNotFound_ReturnsFalse() {
//        // Arrange
//        when(courseRepo.findById(anyLong())).thenReturn(Optional.empty());
//
//        // Act
//        boolean result = courseService.isOwner(1L, 1L);
//
//        // Assert
//        assertFalse(result);
//    }
//}
