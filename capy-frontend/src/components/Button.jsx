export default function Button({ onClick, className = '', children }) {
  const buttonStyle = {
    marginTop: 'var(--space-3)',
    width: '100%',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '0.75rem',
    padding: '0.55rem 1rem',
    borderRadius: '12px',
    background: 'var(--riverbank-cream)',
    border: '1px solid rgba(200, 162, 107, 0.2)',
    color: 'var(--text)',
    fontFamily: 'var(--font-body)',
    fontSize: 'var(--text-sm)',
    cursor: 'pointer',
    transition: 'all 0.25s cubic-bezier(0.2, 0, 0, 1)',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.05)',
  }

  const badgeStyle = {
    width: '28px',
    height: '28px',
    borderRadius: '999px',
    display: 'grid',
    placeItems: 'center',
    border: '1px solid var(--border)',
    color: 'var(--mudbank)',
    fontWeight: '700',
    fontSize: '0.95rem',
  }

  return (
    <button
      type="button"
      style={buttonStyle}
      className={className}
      onClick={onClick}
      onMouseEnter={(e) => {
        e.target.style.background = '#f0e7dd'
        e.target.style.boxShadow = '0 4px 16px rgba(0, 0, 0, 0.08)'
        e.target.style.transform = 'translateY(-1px)'
        e.target.style.borderColor = 'rgba(200, 162, 107, 0.4)'
      }}
      onMouseLeave={(e) => {
        e.target.style.background = 'var(--riverbank-cream)'
        e.target.style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.05)'
        e.target.style.transform = 'translateY(0)'
        e.target.style.borderColor = 'rgba(200, 162, 107, 0.2)'
      }}
    >
      {children || (
        <>
          <img 
            src="/images/google-logo.png" 
            alt="Google" 
            style={{ width: '20px', height: '20px' }}
          />
          Continue with Google
        </>
      )}
    </button>
  )
}
