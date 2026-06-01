export const getInitialSkills = async () => {
    try {
        const response = await fetch('/api/profile/me', {
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch profile');
        }

        const profile = await response.json();
        return {
            skillsTeach: profile.offeredSkills || [],
            skillsLearn: profile.requestedSkills || []
        };
    } catch (error) {
        console.error("Error loading skills:", error);
        return { skillsTeach: [], skillsLearn: [] };
    }
}

export const updateSkills = async ({ offeredSkills, requestedSkills }) => {
    try {
        const response = await fetch('/api/profile/skills', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ offeredSkills, requestedSkills })
        })

        if (!response.ok) {
            throw new Error('Failed to update skills')
        }

        return await response.json()
    } catch (error) {
        console.error('Error updating skills:', error)
        return null
    }
}
