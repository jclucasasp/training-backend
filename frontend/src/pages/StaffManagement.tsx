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
import { staffApi } from '@/services/api';
import { StaffResponse, CreateStaffRequest } from '@/types/api';
import { Plus, Pencil, Trash2 } from 'lucide-react';

export default function StaffManagement() {
  const [staff, setStaff] = useState<StaffResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingStaff, setEditingStaff] = useState<StaffResponse | null>(null);
  const [formData, setFormData] = useState<CreateStaffRequest>({
    email: '',
    password: '',
    role: 'ADMIN',
    isActive: true,
  });
  const { toast } = useToast();

  const fetchStaff = async (pageNum = 0) => {
    try {
      setLoading(true);
      const response = await staffApi.getAll(pageNum, 10);
      setStaff(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to fetch staff members',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStaff();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingStaff) {
        await staffApi.update(editingStaff.id, formData);
        toast({
          title: 'Success',
          description: 'Staff member updated successfully',
        });
      } else {
        await staffApi.create(formData);
        toast({
          title: 'Success',
          description: 'Staff member created successfully',
        });
      }
      setIsDialogOpen(false);
      fetchStaff(page);
      resetForm();
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to save staff member',
        variant: 'destructive',
      });
    }
  };

  const handleEdit = (staffMember: StaffResponse) => {
    setEditingStaff(staffMember);
    setFormData({
      email: staffMember.email,
      password: '', // Don't pre-fill password
      role: staffMember.role,
      isActive: staffMember.isActive,
    });
    setIsDialogOpen(true);
  };

  const resetForm = () => {
    setEditingStaff(null);
    setFormData({
      email: '',
      password: '',
      role: 'ADMIN',
      isActive: true,
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
          <h1 className="text-4xl font-bold tracking-tight">Staff Management</h1>
          <p className="text-muted-foreground mt-2">
            Manage your organization's staff members and their roles
          </p>
        </div>
        <Button onClick={handleOpenDialog}>
          <Plus className="mr-2 h-4 w-4" />
          Add Staff Member
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Staff Members</CardTitle>
          <CardDescription>
            A list of all staff members in your organization
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
                    <TableHead>Email</TableHead>
                    <TableHead>Role</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {staff.map((member) => (
                    <TableRow key={member.id}>
                      <TableCell>{member.email}</TableCell>
                      <TableCell>{member.role}</TableCell>
                      <TableCell>
                        {member.isActive ? (
                          <span className="text-green-500">Active</span>
                        ) : (
                          <span className="text-red-500">Inactive</span>
                        )}
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleEdit(member)}
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
                        fetchStaff(page - 1);
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
                        fetchStaff(page + 1);
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
            <DialogTitle>
              {editingStaff ? 'Edit Staff Member' : 'Add New Staff Member'}
            </DialogTitle>
            <DialogDescription>
              {editingStaff
                ? 'Update the staff member details below'
                : 'Fill in the details to add a new staff member'}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmit}>
            <div className="space-y-4">
              <div>
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  value={formData.email}
                  onChange={(e) =>
                    setFormData({ ...formData, email: e.target.value })
                  }
                  required
                />
              </div>
              <div>
                <Label htmlFor="password">
                  {editingStaff ? 'New Password (optional)' : 'Password'}
                </Label>
                <Input
                  id="password"
                  type="password"
                  value={formData.password}
                  onChange={(e) =>
                    setFormData({ ...formData, password: e.target.value })
                  }
                  required={!editingStaff}
                />
              </div>
              <div>
                <Label htmlFor="role">Role</Label>
                <select
                  id="role"
                  value={formData.role}
                  onChange={(e) =>
                    setFormData({ ...formData, role: e.target.value })
                  }
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                >
                  <option value="ADMIN">Admin</option>
                  <option value="INSTRUCTOR">Instructor</option>
                  <option value="MANAGER">Manager</option>
                </select>
              </div>
              <div className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  id="isActive"
                  checked={formData.isActive}
                  onChange={(e) =>
                    setFormData({ ...formData, isActive: e.target.checked })
                  }
                  className="h-4 w-4"
                />
                <Label htmlFor="isActive">Active</Label>
              </div>
            </div>
            <DialogFooter className="mt-4">
              <Button type="submit">
                {editingStaff ? 'Update' : 'Create'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
