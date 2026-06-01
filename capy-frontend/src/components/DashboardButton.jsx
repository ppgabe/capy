import { useState } from 'react'

export default function DashboardButton({ onClick, className = '', children, disabled = false }) {
  const [isActive, setIsActive] = useState(false)
  const isGoOnline = children === 'Go Online'

  const buttonStyle = {
    width: 'auto',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '0.75rem',
    padding: '0.4rem 0.9rem',
    borderRadius: '24px',
    background: 'var(--riverbank-cream)',
    border: '1px solid var(--mudbank)',
    color: 'var(--text)',
    fontFamily: 'var(--font-body)',
    fontSize: 'var(--text-sm)',
    cursor: disabled ? 'not-allowed' : 'pointer',
    opacity: disabled ? 0.6 : 1,
    transition: 'all 0.25s ease',
    whiteSpace: 'nowrap',
    fontWeight: isGoOnline ? '700' : '400',
  }

  const circleStyle = {
    width: '8px',
    height: '8px',
    borderRadius: '50%',
    background: isActive ? '#7bae5c' : '#e8d5b7',
    transition: 'all 0.25s ease',
  }

  const handleClick = (e) => {
    if (disabled) return
    if (isGoOnline) {
      setIsActive(!isActive)
    }
    if (onClick) onClick(e)
  }

  return (
    <button
      type="button"
      style={buttonStyle}
      className={className}
      onClick={handleClick}
      disabled={disabled}
      onMouseEnter={(e) => {
        if (disabled) return
        e.target.style.background = '#efe7dd'
        e.target.style.boxShadow = '0 4px 16px rgba(0, 0, 0, 0.08)'
        e.target.style.transform = 'translateY(-1px)'
      }}
      onMouseLeave={(e) => {
        if (disabled) return
        e.target.style.background = 'var(--riverbank-cream)'
        e.target.style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.05)'
        e.target.style.transform = 'translateY(0)'
      }}
    >
      {children}
      {isGoOnline && <span style={circleStyle}></span>}
    </button>
  )
}
