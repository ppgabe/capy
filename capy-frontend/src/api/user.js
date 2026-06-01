export const getUserData = async () => {
    try {
        const response = await fetch('/api/profile/me');
        if (!response.ok) {
            throw new Error('Not authenticated');
        }
        return await response.json();
    } catch (error) {
        console.error("Error fetching user:", error);
        return null; // Handle redirect to login on the UI side if null
    }
}
