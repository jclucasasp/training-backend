import React, { useEffect, useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/components/ui/use-toast';
import { orgApi } from '@/services/api';
import { OrganisationResponse, ProfileRequest } from '@/types/api';
import { Key, Save, Copy, Check } from 'lucide-react';

export default function Settings() {
  const [orgData, setOrgData] = useState<OrganisationResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [apiKey, setApiKey] = useState('');
  const [copied, setCopied] = useState(false);
  const [profileData, setProfileData] = useState<ProfileRequest>({
    registrationNumber: '',
    vatNumber: '',
    displayName: '',
  });
  const { toast } = useToast();

  useEffect(() => {
    fetchOrgData();
  }, []);

  const fetchOrgData = async () => {
    try {
      setLoading(true);
      const data = await orgApi.getDetails();
      setOrgData(data);
      setApiKey(data.apiKey || '');
      setProfileData({
        registrationNumber: data.registrationNumber || '',
        vatNumber: data.vatNumber || '',
        displayName: data.orgName || '',
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to fetch organization details',
        variant: 'destructive',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateApiKey = async () => {
    try {
      const response = await orgApi.generateApiKey();
      setApiKey(response.apiKey);
      toast({
        title: 'Success',
        description: 'New API key generated. Please save it securely.',
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to generate API key',
        variant: 'destructive',
      });
    }
  };

  const handleCopyApiKey = () => {
    navigator.clipboard.writeText(apiKey);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleProfileUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await orgApi.updateProfile(profileData);
      toast({
        title: 'Success',
        description: 'Profile updated successfully',
      });
      fetchOrgData();
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to update profile',
        variant: 'destructive',
      });
    }
  };

  if (loading) {
    return <div className="flex items-center justify-center h-screen">Loading...</div>;
  }

  return (
    <div className="p-8 space-y-8">
      <div>
        <h1 className="text-4xl font-bold tracking-tight">Settings</h1>
        <p className="text-muted-foreground mt-2">
          Manage your organization profile and API access
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Organization Profile</CardTitle>
            <CardDescription>
              Update your organization's business information
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleProfileUpdate} className="space-y-4">
              <div>
                <Label htmlFor="displayName">Display Name</Label>
                <Input
                  id="displayName"
                  value={profileData.displayName}
                  onChange={(e) =>
                    setProfileData({ ...profileData, displayName: e.target.value })
                  }
                />
              </div>
              <div>
                <Label htmlFor="registrationNumber">Registration Number</Label>
                <Input
                  id="registrationNumber"
                  value={profileData.registrationNumber}
                  onChange={(e) =>
                    setProfileData({
                      ...profileData,
                      registrationNumber: e.target.value,
                    })
                  }
                />
              </div>
              <div>
                <Label htmlFor="vatNumber">VAT Number</Label>
                <Input
                  id="vatNumber"
                  value={profileData.vatNumber}
                  onChange={(e) =>
                    setProfileData({ ...profileData, vatNumber: e.target.value })
                  }
                />
              </div>
              <Button type="submit">
                <Save className="mr-2 h-4 w-4" />
                Save Changes
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>API Key Management</CardTitle>
            <CardDescription>
              Generate and manage your API access keys
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="apiKey">Current API Key</Label>
              <div className="flex gap-2">
                <Input
                  id="apiKey"
                  value={apiKey}
                  readOnly
                  className="font-mono"
                />
                <Button
                  type="button"
                  variant="outline"
                  size="icon"
                  onClick={handleCopyApiKey}
                  disabled={!apiKey}
                >
                  {copied ? (
                    <Check className="h-4 w-4" />
                  ) : (
                    <Copy className="h-4 w-4" />
                  )}
                </Button>
              </div>
              <p className="text-sm text-muted-foreground mt-2">
                Keep this key secure. It provides full access to your organization's data.
              </p>
            </div>
            <Button onClick={handleGenerateApiKey}>
              <Key className="mr-2 h-4 w-4" />
              Generate New API Key
            </Button>
          </CardContent>
        </Card>
      </div>

      {orgData && (
        <Card>
          <CardHeader>
            <CardTitle>Subscription Details</CardTitle>
            <CardDescription>
              Your current subscription plan and status
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid gap-4 md:grid-cols-2">
              <div>
                <p className="text-sm font-medium">Plan</p>
                <p className="text-2xl font-bold">{orgData.subscriptionPlan}</p>
              </div>
              <div>
                <p className="text-sm font-medium">Status</p>
                <p className={`text-2xl font-bold ${orgData.subscriptionStatus ? 'text-green-500' : 'text-red-500'}`}>
                  {orgData.subscriptionStatus ? 'Active' : 'Inactive'}
                </p>
              </div>
              <div>
                <p className="text-sm font-medium">Start Date</p>
                <p className="text-lg">
                  {new Date(orgData.subscriptionStartDate).toLocaleDateString()}
                </p>
              </div>
              <div>
                <p className="text-sm font-medium">End Date</p>
                <p className="text-lg">
                  {new Date(orgData.subscriptionEndDate).toLocaleDateString()}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
