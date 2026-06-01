export const joinMatchmaking = async () => {
    try {
        // 1. Tell the backend we are looking for a match
        const response = await fetch('/api/matchmaking/join', {
            method: 'POST'
        });

        if (!response.ok) throw new Error('Failed to join queue');

        const data = await response.json();
        return data.matchQueueTopic; // Expected to be "/queue/match/{id}"
    } catch (error) {
        console.error("Matchmaking error:", error);
        return null;
    }
}
