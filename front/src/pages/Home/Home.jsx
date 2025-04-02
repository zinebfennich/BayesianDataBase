import React, { useEffect } from 'react'
import { Link } from 'react-router-dom'
import clsx from 'clsx';

import { timeline } from '../../constants';
import { links } from '../../constants';
import timelineAnimation from '../../utils/timelineAnimation';
import Timeline from '../../components/Timeline/Timeline';

/* style */
import styles from './Home.module.css';

const Home = () => {
    useEffect(() => {
        timelineAnimation();
    });
    
    return (
    <>
        <header className={clsx(styles.header, "container")}>
            <div className={styles.header__logo_container}>
                <img className={styles.header__logo} src="./images/logo.png" alt="" />
                <span className={styles.header__title}>Bayesian For IMDb</span>
            </div>
        
            <Link className={styles.header__btn} to={links.dashboard.path}>Enter App  
                <svg className={styles.header__btn_arrow} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 4.5 21 12m0 0-7.5 7.5M21 12H3" />
                </svg>
            </Link>
        </header>
        
        <main className={styles.main}>
            {/* HERO */}
            <section className={clsx("container", styles.hero)}>
                <h2 className='title'>Analyse</h2>
                
                <h1 className={styles.hero__title}>Boost Your Data Insights with Bayesian Database.</h1>
                
                <p className={styles.hero__description}>A powerful Big Data solution using <strong>Machine Learning</strong> to collect, analyze, and optimize your company data like never before.</p>
                
                <Link className={styles.hero__btn} to={links.dashboard.path}>
                    <svg className={styles.hero__btn_icon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M15.59 14.37a6 6 0 0 1-5.84 7.38v-4.8m5.84-2.58a14.98 14.98 0 0 0 6.16-12.12A14.98 14.98 0 0 0 9.631 8.41m5.96 5.96a14.926 14.926 0 0 1-5.841 2.58m-.119-8.54a6 6 0 0 0-7.381 5.84h4.8m2.581-5.84a14.927 14.927 0 0 0-2.58 5.84m2.699 2.7c-.103.021-.207.041-.311.06a15.09 15.09 0 0 1-2.448-2.448 14.9 14.9 0 0 1 .06-.312m-2.24 2.39a4.493 4.493 0 0 0-1.757 4.306 4.493 4.493 0 0 0 4.306-1.758M16.5 9a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0Z" />
                    </svg>
                    Try It Now  
                </Link>
            </section>

            {/* TIMELINE */}
            <section className="section">
                <h2 className='container title'>Timeline</h2>
                
                <div className={styles.timeline}>
                    <div className={styles.timeline__baseline}>
                        {
                            timeline.map(({id, date, progress}, i) => (
                                <Timeline
                                key={id}
                                date={date}
                                progress={progress} 
                                last={i + 1 == timeline.length ? true : false}/>
                            ))
                        }
                    </div>
                </div>
            </section>

            {/* ABOUT */}
            <section className={clsx('container section', styles.about)}>
                <h2 className='title'>about</h2>
                
                <div>
                    <h3 className={styles.about__title}>Vision</h3>
                    <p className={styles.about__description}>Traditional databases like Oracle, SQL Server, and DB2 were designed for transactional processing, concurrent access, and system performance metrics. However, they are not optimized for massive, unstructured data analysis, which is now essential for modern businesses.</p>
                    <p className={styles.about__description}>Newer technologies like Hadoop introduced shading and distributed computing, improving large-scale data processing. But businesses need more than just speed—they need intelligent, adaptive insights to stay ahead.</p>
                </div>

                <div>
                    <h3 className={styles.about__title}>Goal</h3>
                    <p className={styles.about__description}>Our mission is to bridge the gap between <strong>traditional</strong> databases and <strong>modern</strong> Big Data solutions. Bayesian Database is designed to handle enterprise-level data collection and analysis using <strong>Machine Learning</strong>, turning raw information into meaningful, predictive insights</p>
                </div>

                <div>
                    <h3 className={styles.about__title}>Resolve</h3>
                    <p className={styles.about__description}>With Bayesian Database, businesses can:</p>
                    <ul className={styles.about__goals}>
                        <li className={styles.about__goal}>
                            <strong>Efficiently</strong> process structured & unstructured data
                        </li>
                        <li className={styles.about__goal}>
                            <strong>Leverage</strong> AI-driven analytics for smarter decisions
                        </li>
                        <li className={styles.about__goal}>
                            <strong>Scale</strong> effortlessly with Big Data technology
                        </li>
                    </ul>
                </div>
            </section>

            {/* MENTOR */}
            <section className='container section'>
                <h2 className='title'>Mentor</h2>
                <div className={styles.mentor_container}>
                    <h3 className={styles.mentor__title}>This project can't be done without them.</h3>
                    <p className={styles.mentor__description}>Lorem ipsum dolor sit amet consectetur, adipisicing elit. Distinctio at, maiores nisi hic ducimus, labore pariatur adipisci omnis, facere voluptatum ratione. Quos cumque, obcaecati velit quam doloremque ea provident ab.</p>
                    
                    <div className={styles.mentor__card}>
                        <svg className={styles.mentor__icon} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                            <path fill-rule="evenodd" d="M7.5 6a4.5 4.5 0 1 1 9 0 4.5 4.5 0 0 1-9 0ZM3.751 20.105a8.25 8.25 0 0 1 16.498 0 .75.75 0 0 1-.437.695A18.683 18.683 0 0 1 12 22.5c-2.786 0-5.433-.608-7.812-1.7a.75.75 0 0 1-.437-.695Z" clip-rule="evenodd" />
                        </svg>

                        <div className={styles.mentor__info}>
                            <h4 className={styles.mentor__info_name}>Jastrebic Dragutin</h4>
                            <p className={styles.mentor__info_description}>Lorem ipsum dolor sit amet consectetur adipisicing elit. Laudantium quos, animi repudiandae commodi tenetur officia minus nesciunt neque quod accusamus cupiditate veniam quis aliquid incidunt debitis rerum, adipisci voluptatem reprehenderit.</p>
                        </div>
                    </div>

                    <div className={styles.mentor__card}>
                        <svg className={styles.mentor__icon} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                            <path fill-rule="evenodd" d="M7.5 6a4.5 4.5 0 1 1 9 0 4.5 4.5 0 0 1-9 0ZM3.751 20.105a8.25 8.25 0 0 1 16.498 0 .75.75 0 0 1-.437.695A18.683 18.683 0 0 1 12 22.5c-2.786 0-5.433-.608-7.812-1.7a.75.75 0 0 1-.437-.695Z" clip-rule="evenodd" />
                        </svg>

                        <div className={styles.mentor__info}>
                            <h4 className={styles.mentor__info_name}>Koviljka</h4>
                            <p className={styles.mentor__info_description}>Lorem ipsum dolor sit amet consectetur adipisicing elit. Obcaecati placeat laborum eaque officiis molestiae, nulla dolor nostrum omnis saepe culpa, sapiente, eum delectus a rem architecto? Ut sint provident ab.</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* MEMBER */}
            <section className='container section'>
                <h2 className='title'>Members</h2>
                <h3 className={styles.member__title}>TBD</h3>
                <p className={styles.member__description}>Lorem, ipsum dolor sit amet consectetur adipisicing elit. Cumque, reprehenderit deserunt rem modi unde qui, at quod accusantium fugiat ut quam, temporibus dignissimos! Adipisci quia in odio, non dolor iste?</p>

                <div className={styles.member__container}>
                    <div>
                        <svg className={styles.member__icon} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                            <path fill-rule="evenodd" d="M7.5 6a4.5 4.5 0 1 1 9 0 4.5 4.5 0 0 1-9 0ZM3.751 20.105a8.25 8.25 0 0 1 16.498 0 .75.75 0 0 1-.437.695A18.683 18.683 0 0 1 12 22.5c-2.786 0-5.433-.608-7.812-1.7a.75.75 0 0 1-.437-.695Z" clip-rule="evenodd" />
                        </svg>
                        <h3 className={styles.member__name}>BARAHIMI Imane</h3>
                        <p className={styles.member__info}>Back-end developer & redactor</p>
                    </div>

                    <div>
                        <svg className={styles.member__icon} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                            <path fill-rule="evenodd" d="M7.5 6a4.5 4.5 0 1 1 9 0 4.5 4.5 0 0 1-9 0ZM3.751 20.105a8.25 8.25 0 0 1 16.498 0 .75.75 0 0 1-.437.695A18.683 18.683 0 0 1 12 22.5c-2.786 0-5.433-.608-7.812-1.7a.75.75 0 0 1-.437-.695Z" clip-rule="evenodd" />
                        </svg>
                        <h3 className={styles.member__name}>BEN SLAMA Sana</h3>
                        <p className={styles.member__info}>Back-end developer & redactor</p>
                    </div>

                    <div>
                        <svg className={styles.member__icon} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                            <path fill-rule="evenodd" d="M7.5 6a4.5 4.5 0 1 1 9 0 4.5 4.5 0 0 1-9 0ZM3.751 20.105a8.25 8.25 0 0 1 16.498 0 .75.75 0 0 1-.437.695A18.683 18.683 0 0 1 12 22.5c-2.786 0-5.433-.608-7.812-1.7a.75.75 0 0 1-.437-.695Z" clip-rule="evenodd" />
                        </svg>
                        <h3 className={styles.member__name}>CHEN Roland</h3>
                        <p className={styles.member__info}>Front-end designer & developer & redactor</p>
                    </div>

                    <div>
                        <svg className={styles.member__icon} xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
                            <path fill-rule="evenodd" d="M7.5 6a4.5 4.5 0 1 1 9 0 4.5 4.5 0 0 1-9 0ZM3.751 20.105a8.25 8.25 0 0 1 16.498 0 .75.75 0 0 1-.437.695A18.683 18.683 0 0 1 12 22.5c-2.786 0-5.433-.608-7.812-1.7a.75.75 0 0 1-.437-.695Z" clip-rule="evenodd" />
                        </svg>
                        <h3 className={styles.member__name}>FENNICH Zineb</h3>
                        <p className={styles.member__info}>Back-end developer & redactor</p>
                    </div>
                </div>
            </section>

            {/* TECHNOLOGIE */}
            <section className='container section'>
                <h2 className='title'>Technologies</h2>
                
                <div className={styles.technologies}>
                    <i className={clsx("lni lni-react", styles.item, styles.item1)}></i>
                    <i className={clsx("lni lni-python", styles.item, styles.item2)}></i>
                    <i className={clsx("lni lni-javascript", styles.item, styles.item3)}></i>
                    <i className={clsx("lni lni-java", styles.item, styles.item4)}></i>
                    <i className={clsx("lni lni-postgresql", styles.item, styles.item5)}></i>
                </div>
            </section>

            {/* CTA */}
            <section className='container section'>
                <h2 className='title'>CTA</h2>
                <h3 className={styles.cta__title}>Ready to revolutionize your data strategy? Explore Bayesian Database today !</h3>

                <a className={styles.cta__btn} target="_blank"  href="https://github.com/zinebfennich/BayesianDataBase">
                    <svg className={styles.cta__btn_icon} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth="1.5" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M15.59 14.37a6 6 0 0 1-5.84 7.38v-4.8m5.84-2.58a14.98 14.98 0 0 0 6.16-12.12A14.98 14.98 0 0 0 9.631 8.41m5.96 5.96a14.926 14.926 0 0 1-5.841 2.58m-.119-8.54a6 6 0 0 0-7.381 5.84h4.8m2.581-5.84a14.927 14.927 0 0 0-2.58 5.84m2.699 2.7c-.103.021-.207.041-.311.06a15.09 15.09 0 0 1-2.448-2.448 14.9 14.9 0 0 1 .06-.312m-2.24 2.39a4.493 4.493 0 0 0-1.757 4.306 4.493 4.493 0 0 0 4.306-1.758M16.5 9a1.5 1.5 0 1 1-3 0 1.5 1.5 0 0 1 3 0Z" />
                    </svg>
                    Check project
                </a>
            </section>
        </main>
        
        <footer className={clsx("container", styles.footer)}>
            <span className={styles.footer__text}>~ project L3K1 ~</span>
        </footer>
    </>
  )
}

export default Home