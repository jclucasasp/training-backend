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
import { courseApi } from '@/services/api';
import { CourseResponse, CourseCreateRequest, CourseUpdateRequest, ModuleRequest, SectionRequest } from '@/types/api';
import { Plus, Pencil, Trash2, ChevronDown, ChevronRight } from 'lucide-react';

export default function CourseManagement() {
  const [courses, setCourses] = useState<CourseResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingCourse, setEditingCourse] = useState<CourseResponse | null>(null);
  const [formData, setFormData] = useState<CourseCreateRequest>({
    name: '',
    description: '',
    difficultyTypes: 'BEGINNER',
    tags: '',
    imageUrl: '',
    modules: [],
  });
  const [expandedModules, setExpandedModules] = useState<Set<number>>(new Set());
  const { toast } = useToast();

  const fetchCourses = async (pageNum = 0) => {
    try {
      setLoading(true);
      const response = await courseApi.getAll(pageNum, 10);
      setCourses(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to fetch courses',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingCourse) {
        const updateData: CourseUpdateRequest = {
          name: formData.name,
          description: formData.description,
          difficultyTypes: formData.difficultyTypes,
          tags: formData.tags,
          imageUrl: formData.imageUrl,
          modules: formData.modules,
        };
        await courseApi.update(editingCourse.id, updateData);
        toast({
          title: 'Success',
          description: 'Course updated successfully',
        });
      } else {
        await courseApi.create(formData);
        toast({
          title: 'Success',
          description: 'Course created successfully',
        });
      }
      setIsDialogOpen(false);
      fetchCourses(page);
      resetForm();
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to save course',
        variant: 'destructive',
      });
    }
  };

  const handleEdit = (course: CourseResponse) => {
    setEditingCourse(course);
    setFormData({
      name: course.name,
      description: course.description,
      difficultyTypes: course.difficulty as any,
      tags: course.tags,
      imageUrl: course.imageUrl || '',
      modules: [],
    });
    setIsDialogOpen(true);
  };

  const resetForm = () => {
    setEditingCourse(null);
    setFormData({
      name: '',
      description: '',
      difficultyTypes: 'BEGINNER',
      tags: '',
      imageUrl: '',
      modules: [],
    });
    setExpandedModules(new Set());
  };

  const handleOpenDialog = () => {
    resetForm();
    setIsDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setIsDialogOpen(false);
    resetForm();
  };

  const toggleModule = (index: number) => {
    const newExpanded = new Set(expandedModules);
    if (newExpanded.has(index)) {
      newExpanded.delete(index);
    } else {
      newExpanded.add(index);
    }
    setExpandedModules(newExpanded);
  };

  const addModule = () => {
    setFormData({
      ...formData,
      modules: [
        ...formData.modules,
        {
          name: '',
          description: '',
          sections: [],
        },
      ],
    });
  };

  const updateModule = (index: number, field: keyof ModuleRequest, value: any) => {
    const newModules = [...formData.modules];
    newModules[index] = { ...newModules[index], [field]: value };
    setFormData({ ...formData, modules: newModules });
  };

  const removeModule = (index: number) => {
    const newModules = formData.modules.filter((_, i) => i !== index);
    setFormData({ ...formData, modules: newModules });
  };

  const addSection = (moduleIndex: number) => {
    const newModules = [...formData.modules];
    newModules[moduleIndex] = {
      ...newModules[moduleIndex],
      sections: [
        ...newModules[moduleIndex].sections,
        {
          title: '',
          content: '',
          duration: 0,
          orderIndex: newModules[moduleIndex].sections.length,
          tags: '',
        },
      ],
    };
    setFormData({ ...formData, modules: newModules });
  };

  const updateSection = (
    moduleIndex: number,
    sectionIndex: number,
    field: keyof SectionRequest,
    value: any
  ) => {
    const newModules = [...formData.modules];
    newModules[moduleIndex].sections[sectionIndex] = {
      ...newModules[moduleIndex].sections[sectionIndex],
      [field]: value,
    };
    setFormData({ ...formData, modules: newModules });
  };

  const removeSection = (moduleIndex: number, sectionIndex: number) => {
    const newModules = [...formData.modules];
    newModules[moduleIndex].sections = newModules[moduleIndex].sections.filter(
      (_, i) => i !== sectionIndex
    );
    setFormData({ ...formData, modules: newModules });
  };

  return (
    <div className="p-8 space-y-8">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-4xl font-bold tracking-tight">Course Management</h1>
          <p className="text-muted-foreground mt-2">
            Create and manage your courses, modules, and sections
          </p>
        </div>
        <Button onClick={handleOpenDialog}>
          <Plus className="mr-2 h-4 w-4" />
          Create Course
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Courses</CardTitle>
          <CardDescription>
            A list of all available courses
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
                    <TableHead>Name</TableHead>
                    <TableHead>Description</TableHead>
                    <TableHead>Difficulty</TableHead>
                    <TableHead>Tags</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {courses.map((course) => (
                    <TableRow key={course.id}>
                      <TableCell className="font-medium">{course.name}</TableCell>
                      <TableCell>{course.description}</TableCell>
                      <TableCell>{course.difficulty}</TableCell>
                      <TableCell>{course.tags}</TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleEdit(course)}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                      </TableCell>
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
                        fetchCourses(page - 1);
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
                        fetchCourses(page + 1);
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
        <DialogContent className="max-w-5xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {editingCourse ? 'Edit Course' : 'Create New Course'}
            </DialogTitle>
            <DialogDescription>
              {editingCourse
                ? 'Update the course details below'
                : 'Fill in the details to create a new course'}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmit}>
            <div className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="name">Course Name</Label>
                  <Input
                    id="name"
                    value={formData.name}
                    onChange={(e) =>
                      setFormData({ ...formData, name: e.target.value })
                    }
                    required
                  />
                </div>
                <div>
                  <Label htmlFor="difficulty">Difficulty</Label>
                  <select
                    id="difficulty"
                    value={formData.difficultyTypes}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        difficultyTypes: e.target.value as any,
                      })
                    }
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                  >
                    <option value="BEGINNER">Beginner</option>
                    <option value="INTERMEDIATE">Intermediate</option>
                    <option value="ADVANCED">Advanced</option>
                  </select>
                </div>
              </div>

              <div>
                <Label htmlFor="description">Description</Label>
                <textarea
                  id="description"
                  value={formData.description}
                  onChange={(e) =>
                    setFormData({ ...formData, description: e.target.value })
                  }
                  className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                  required
                />
              </div>

              <div>
                <Label htmlFor="tags">Tags</Label>
                <Input
                  id="tags"
                  value={formData.tags}
                  onChange={(e) =>
                    setFormData({ ...formData, tags: e.target.value })
                  }
                  placeholder="e.g., JavaScript, React, TypeScript"
                  required
                />
              </div>

              <div>
                <Label htmlFor="imageUrl">Image URL (optional)</Label>
                <Input
                  id="imageUrl"
                  value={formData.imageUrl}
                  onChange={(e) =>
                    setFormData({ ...formData, imageUrl: e.target.value })
                  }
                  placeholder="https://example.com/course-image.jpg"
                />
              </div>

              <div className="border-t pt-6">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold">Modules</h3>
                  <Button type="button" onClick={addModule} variant="outline" size="sm">
                    <Plus className="mr-2 h-4 w-4" />
                    Add Module
                  </Button>
                </div>

                {formData.modules.map((module, moduleIndex) => (
                  <div key={moduleIndex} className="border rounded-lg p-4 mb-4 space-y-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon"
                          onClick={() => toggleModule(moduleIndex)}
                        >
                          {expandedModules.has(moduleIndex) ? (
                            <ChevronDown className="h-4 w-4" />
                          ) : (
                            <ChevronRight className="h-4 w-4" />
                          )}
                        </Button>
                        <span className="font-medium">Module {moduleIndex + 1}</span>
                      </div>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        onClick={() => removeModule(moduleIndex)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label htmlFor={`module-name-${moduleIndex}`}>Module Name</Label>
                        <Input
                          id={`module-name-${moduleIndex}`}
                          value={module.name}
                          onChange={(e) =>
                            updateModule(moduleIndex, 'name', e.target.value)
                          }
                          required
                        />
                      </div>
                      <div>
                        <Label htmlFor={`module-desc-${moduleIndex}`}>Description</Label>
                        <Input
                          id={`module-desc-${moduleIndex}`}
                          value={module.description}
                          onChange={(e) =>
                            updateModule(moduleIndex, 'description', e.target.value)
                          }
                          required
                        />
                      </div>
                    </div>

                    {expandedModules.has(moduleIndex) && (
                      <div className="border-t pt-4 mt-4">
                        <div className="flex items-center justify-between mb-4">
                          <h4 className="text-sm font-semibold">Sections</h4>
                          <Button
                            type="button"
                            onClick={() => addSection(moduleIndex)}
                            variant="outline"
                            size="sm"
                          >
                            <Plus className="mr-2 h-4 w-4" />
                            Add Section
                          </Button>
                        </div>

                        {module.sections.map((section, sectionIndex) => (
                          <div
                            key={sectionIndex}
                            className="border rounded p-4 mb-4 space-y-4 bg-muted/20"
                          >
                            <div className="flex items-center justify-between">
                              <span className="text-sm font-medium">Section {sectionIndex + 1}</span>
                              <Button
                                type="button"
                                variant="ghost"
                                size="icon"
                                onClick={() => removeSection(moduleIndex, sectionIndex)}
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                              <div>
                                <Label htmlFor={`section-title-${moduleIndex}-${sectionIndex}`}>
                                  Title
                                </Label>
                                <Input
                                  id={`section-title-${moduleIndex}-${sectionIndex}`}
                                  value={section.title}
                                  onChange={(e) =>
                                    updateSection(moduleIndex, sectionIndex, 'title', e.target.value)
                                  }
                                  required
                                />
                              </div>
                              <div>
                                <Label htmlFor={`section-duration-${moduleIndex}-${sectionIndex}`}>
                                  Duration (minutes)
                                </Label>
                                <Input
                                  id={`section-duration-${moduleIndex}-${sectionIndex}`}
                                  type="number"
                                  value={section.duration}
                                  onChange={(e) =>
                                    updateSection(
                                      moduleIndex,
                                      sectionIndex,
                                      'duration',
                                      parseInt(e.target.value) || 0
                                    )
                                  }
                                  required
                                />
                              </div>
                            </div>

                            <div>
                              <Label htmlFor={`section-content-${moduleIndex}-${sectionIndex}`}>
                                Content
                              </Label>
                              <textarea
                                id={`section-content-${moduleIndex}-${sectionIndex}`}
                                value={section.content}
                                onChange={(e) =>
                                  updateSection(moduleIndex, sectionIndex, 'content', e.target.value)
                                }
                                className="flex min-h-[100px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                                required
                              />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                              <div>
                                <Label htmlFor={`section-tags-${moduleIndex}-${sectionIndex}`}>
                                  Tags
                                </Label>
                                <Input
                                  id={`section-tags-${moduleIndex}-${sectionIndex}`}
                                  value={section.tags}
                                  onChange={(e) =>
                                    updateSection(moduleIndex, sectionIndex, 'tags', e.target.value)
                                  }
                                  placeholder="e.g., Theory, Practice"
                                  required
                                />
                              </div>
                              <div>
                                <Label htmlFor={`section-resource-${moduleIndex}-${sectionIndex}`}>
                                  Resource URL (optional)
                                </Label>
                                <Input
                                  id={`section-resource-${moduleIndex}-${sectionIndex}`}
                                  value={section.resourceUrl || ''}
                                  onChange={(e) =>
                                    updateSection(moduleIndex, sectionIndex, 'resourceUrl', e.target.value)
                                  }
                                  placeholder="https://example.com/resource.pdf"
                                />
                              </div>
                            </div>
                          </div>
                        ))}

                        {module.sections.length === 0 && (
                          <div className="text-center py-4 text-sm text-muted-foreground">
                            No sections yet. Click "Add Section" to create one.
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                ))}

                {formData.modules.length === 0 && (
                  <div className="text-center py-8 text-muted-foreground">
                    No modules yet. Click "Add Module" to create one.
                  </div>
                )}
              </div>
            </div>

            <DialogFooter className="mt-6">
              <Button type="submit">
                {editingCourse ? 'Update' : 'Create'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
