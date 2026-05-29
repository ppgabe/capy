export const getUserData = () => {
  const storedName = localStorage.getItem('userName')
  return {
    name: storedName || 'us',
  }
}

export const updateUserName = (newName) => {
  localStorage.setItem('userName', newName)
}
