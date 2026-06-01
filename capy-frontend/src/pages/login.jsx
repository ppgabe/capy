import '../styles/register.css'

export default function Login() {
  return (
    <div className="landing-grid">
      <section className="left-panel stack" aria-labelledby="hero-title">
        <div className="avatar-icon">
          <div className="head"></div>
          <div className="body"></div>
          <div className="triangle"></div>
        </div>

        <h1 id="hero-title">
          Learn by teaching.
          <br />
          Teach by learning.
        </h1>

        <p className="hero-subtext">
          Every user is both a teacher and a student.
        </p>
      </section>

      <div className="right-stack">
        <section className="register-card" aria-labelledby="login-title">
          <h2 id="login-title" className="card__title">
            Ready to learn?
          </h2>
          <p className="card__subtext">
            <em>Join our community of learners and teachers.</em>
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

        <section className="about" aria-labelledby="about-title">
          <h2 id="about-title" className="card__title">
            About Capy
          </h2>
          <p>
            Capy is a peer-to-peer skill exchange platform where knowledge
            flows in both directions. Whether you're a seasoned developer
            wanting to pick up design, or a designer looking to learn to
            code — Capy matches you with someone who can teach you what you
            need, while you teach them what you know. Learning is most
            meaningful when it's mutual.
          </p>
        </section>
      </div>
    </div>
  )
}
