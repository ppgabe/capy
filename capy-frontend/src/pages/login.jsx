import '../styles/login.css'

export default function Login() {
    return (
        <div className="login-container">
            <section className="card login-card" aria-labelledby="login-title">
                <h2 id="login-title" className="card__title">
                    Welcome back.
                </h2>
                <p className="card__subtext">
                    <em>Your learning partner is waiting.</em>
                </p>

                <a href="/oauth2/authorization/google" style={{textDecoration: 'none'}}>
                    <button className="btn-google">
                        <img
                            src="/images/google-logo.png"
                            alt="Google"
                            style={{width: '20px', height: '20px'}}
                        />
                        Continue with Google
                    </button>
                </a>
            </section>
        </div>
    )
}
