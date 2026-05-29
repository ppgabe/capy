import Button from '../components/Button.jsx'
import '../styles/register.css'

export default function Register() {
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
        <section className="register-card stack" aria-labelledby="profile-title">
          <h2 id="profile-title" className="card__title">
            Create your profile
          </h2>
          <p className="card__subtext">
            <em>Tell us what you know and what you want to learn.</em>
          </p>

          <Button />
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
