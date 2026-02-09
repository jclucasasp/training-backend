import React, { useEffect, useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { useToast } from '@/components/ui/use-toast';
import { studentApi, courseApi } from '@/services/api';
import { StudentResponse, StudentEnrollRequest, CourseResponse } from '@/types/api';
import { Plus, GraduationCap } from 'lucide-react';

export default function StudentManagement() {
  const [students, setStudents] = useState<StudentResponse[]>([]);
  const [courses, setCourses] = useState<CourseResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [orgId, setOrgId] = useState<number>(1); // Default org ID - should come from context or API
  const [formData, setFormData] = useState<StudentEnrollRequest>({
    studentNumber: '',
    firstName: '',
    lastName: '',
    courseId: 0,
  });
  const { toast } = useToast();

  const fetchStudents = async (pageNum = 0) => {
    try {
      setLoading(true);
      const response = await studentApi.getAll(orgId, pageNum, 10);
      setStudents(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to fetch students',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  const fetchCourses = async () => {
    try {
      const response = await courseApi.getAll(0, 100);
      setCourses(response.content);
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to fetch courses',
        variant: 'destructive',
      });
    }
  };

  useEffect(() => {
    fetchStudents();
    fetchCourses();
  }, [orgId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await studentApi.enroll(orgId, formData);
      toast({
        title: 'Success',
        description: 'Student enrolled successfully',
      });
      setIsDialogOpen(false);
      fetchStudents(page);
      resetForm();
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to enroll student',
        variant: 'destructive',
      });
    }
  };

  const resetForm = () => {
    setFormData({
      studentNumber: '',
      firstName: '',
      lastName: '',
      courseId: 0,
    });
  };

  const handleOpenDialog = () => {
    resetForm();
    setIsDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setIsDialogOpen(false);
    resetForm();
  };

  return (
    <div className="p-8 space-y-8">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-4xl font-bold tracking-tight">Student Management</h1>
          <p className="text-muted-foreground mt-2">
            Manage student enrollment and track progress
          </p>
        </div>
        <Button onClick={handleOpenDialog}>
          <Plus className="mr-2 h-4 w-4" />
          Enroll Student
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Students</CardTitle>
          <CardDescription>
            A list of all enrolled students
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="text-center py-8">Loading...</div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Student Number</TableHead>
                    <TableHead>First Name</TableHead>
                    <TableHead>Last Name</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {students.map((student) => (
                    <TableRow key={student.id}>
                      <TableCell className="font-medium">{student.studentNumber}</TableCell>
                      <TableCell>{student.firstName}</TableCell>
                      <TableCell>{student.lastName}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>

              {totalPages > 1 && (
                <div className="flex justify-between items-center mt-4">
                  <Button
                    variant="outline"
                    onClick={() => {
                      if (page > 0) {
                        setPage(page - 1);
                        fetchStudents(page - 1);
                      }
                    }}
                    disabled={page === 0}
                  >
                    Previous
                  </Button>
                  <span className="text-sm text-muted-foreground">
                    Page {page + 1} of {totalPages}
                  </span>
                  <Button
                    variant="outline"
                    onClick={() => {
                      if (page < totalPages - 1) {
                        setPage(page + 1);
                        fetchStudents(page + 1);
                      }
                    }}
                    disabled={page >= totalPages - 1}
                  >
                    Next
                  </Button>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>

      <Dialog open={isDialogOpen} onOpenChange={handleCloseDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Enroll New Student</DialogTitle>
            <DialogDescription>
              Fill in the details to enroll a new student
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmit}>
            <div className="space-y-4">
              <div>
                <Label htmlFor="studentNumber">Student Number</Label>
                <Input
                  id="studentNumber"
                  value={formData.studentNumber}
                  onChange={(e) =>
                    setFormData({ ...formData, studentNumber: e.target.value })
                  }
                  required
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="firstName">First Name</Label>
                  <Input
                    id="firstName"
                    value={formData.firstName}
                    onChange={(e) =>
                      setFormData({ ...formData, firstName: e.target.value })
                    }
                    required
                  />
                </div>
                <div>
                  <Label htmlFor="lastName">Last Name</Label>
                  <Input
                    id="lastName"
                    value={formData.lastName}
                    onChange={(e) =>
                      setFormData({ ...formData, lastName: e.target.value })
                    }
                    required
                  />
                </div>
              </div>
              <div>
                <Label htmlFor="course">Course</Label>
                <select
                  id="course"
                  value={formData.courseId}
                  onChange={(e) =>
                    setFormData({ ...formData, courseId: parseInt(e.target.value) })
                  }
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                  required
                >
                  <option value="">Select a course</option>
                  {courses.map((course) => (
                    <option key={course.id} value={course.id}>
                      {course.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <DialogFooter className="mt-4">
              <Button type="submit">Enroll Student</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
