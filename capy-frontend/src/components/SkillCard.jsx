import { useState } from 'react'
import '../styles/skillCard.css'

export default function SkillCard({ title, accentColor, initialSkills }) {
  const [skills, setSkills] = useState(initialSkills)
  const [isInputOpen, setIsInputOpen] = useState(false)
  const [inputValue, setInputValue] = useState('')

  const handleAddSkill = () => {
    const trimmedValue = inputValue.trim()
    if (trimmedValue === '') return

    setSkills([...skills, trimmedValue])
    setInputValue('')
    setIsInputOpen(false)
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleAddSkill()
    } else if (e.key === 'Escape') {
      setIsInputOpen(false)
      setInputValue('')
    }
  }

  const handleRemoveSkill = (index) => {
    setSkills(skills.filter((_, i) => i !== index))
  }

  return (
    <div className="skill-card">
      <h3 className="skill-card-title">{title}</h3>
      <div className="skill-tags">
        {skills.map((skill, index) => (
          <span
            key={index}
            className="skill-tag"
            style={{ backgroundColor: accentColor }}
          >
            {skill}
            <button
              className="skill-tag-remove"
              onClick={() => handleRemoveSkill(index)}
              aria-label="Remove skill"
            >
              ×
            </button>
          </span>
        ))}

        {isInputOpen ? (
          <input
            type="text"
            className="skill-input"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyDown={handleKeyDown}
            onBlur={() => {
              if (inputValue.trim() === '') {
                setIsInputOpen(false)
              }
            }}
            placeholder="Add skill..."
            autoFocus
          />
        ) : (
          <button
            className="skill-add-btn"
            onClick={() => setIsInputOpen(true)}
            aria-label="Add new skill"
          >
            +
          </button>
        )}
      </div>
    </div>
  )
}
