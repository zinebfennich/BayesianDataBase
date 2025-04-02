import React, { useEffect, useState } from 'react' 

import { Outlet, useLocation } from 'react-router-dom'
import { asideLinks, links } from '../../constants/index.jsx'
import Navigation from '../Navigation/Navigation.jsx'

/* style */
import styles from "./Layout.module.css";


function getPath(){
    return useLocation().pathname;
}

function getPageTitle(path){
    for(let link in links){
        if(link.path === path){
            return link.label;
        }
    }
    
    return "Untitled";
}

const Layout = () => {
    const [isOpen, setIsOpen] = useState(false);

    const path = getPath();

    return (
    <div className={styles.container}>
        <aside className={styles.aside}>               
            {
                isOpen?
                <div className={styles.aside_open}>
                    <h2 className={styles.aside__title}>Bayesian Database <br /> For IMDb</h2>
                    <hr className={styles.aside__separator}/>
                    <nav className={styles.aside__nav}>
                        {asideLinks.map(({id, icon, title, links}) => (
                            <Navigation key={id}
                            icon={icon}
                            title={title}
                            links={links}
                            activePath={path}
                            />
                        ))}
                    </nav>
                </div>
                :
                <button className={styles.aside_toggle} onClick={() => setIsOpen(!isOpen)}>
                    <svg className={styles.aside_toggle_icon} viewBox="0 0 25 24" fill="none" xmlns="http://www.w3.org/2000/svg" transform="rotate(0 0 0)">
                        <path fillRule="evenodd" clipRule="evenodd" d="M5.60352 3.25C4.36088 3.25 3.35352 4.25736 3.35352 5.5V8.99998C3.35352 10.2426 4.36087 11.25 5.60352 11.25H9.10352C10.3462 11.25 11.3535 10.2426 11.3535 8.99998V5.5C11.3535 4.25736 10.3462 3.25 9.10352 3.25H5.60352ZM4.85352 5.5C4.85352 5.08579 5.1893 4.75 5.60352 4.75H9.10352C9.51773 4.75 9.85352 5.08579 9.85352 5.5V8.99998C9.85352 9.41419 9.51773 9.74998 9.10352 9.74998H5.60352C5.1893 9.74998 4.85352 9.41419 4.85352 8.99998V5.5Z" fill="#ffffff"/>
                        <path fillRule="evenodd" clipRule="evenodd" d="M5.60352 12.75C4.36088 12.75 3.35352 13.7574 3.35352 15V18.5C3.35352 19.7426 4.36087 20.75 5.60352 20.75H9.10352C10.3462 20.75 11.3535 19.7426 11.3535 18.5V15C11.3535 13.7574 10.3462 12.75 9.10352 12.75H5.60352ZM4.85352 15C4.85352 14.5858 5.1893 14.25 5.60352 14.25H9.10352C9.51773 14.25 9.85352 14.5858 9.85352 15V18.5C9.85352 18.9142 9.51773 19.25 9.10352 19.25H5.60352C5.1893 19.25 4.85352 18.9142 4.85352 18.5V15Z" fill="#ffffff"/>
                        <path fillRule="evenodd" clipRule="evenodd" d="M12.8535 5.5C12.8535 4.25736 13.8609 3.25 15.1035 3.25H18.6035C19.8462 3.25 20.8535 4.25736 20.8535 5.5V8.99998C20.8535 10.2426 19.8462 11.25 18.6035 11.25H15.1035C13.8609 11.25 12.8535 10.2426 12.8535 8.99998V5.5ZM15.1035 4.75C14.6893 4.75 14.3535 5.08579 14.3535 5.5V8.99998C14.3535 9.41419 14.6893 9.74998 15.1035 9.74998H18.6035C19.0177 9.74998 19.3535 9.41419 19.3535 8.99998V5.5C19.3535 5.08579 19.0177 4.75 18.6035 4.75H15.1035Z" fill="#ffffff"/>
                        <path fillRule="evenodd" clipRule="evenodd" d="M15.1035 12.75C13.8609 12.75 12.8535 13.7574 12.8535 15V18.5C12.8535 19.7426 13.8609 20.75 15.1035 20.75H18.6035C19.8462 20.75 20.8535 19.7426 20.8535 18.5V15C20.8535 13.7574 19.8462 12.75 18.6035 12.75H15.1035ZM14.3535 15C14.3535 14.5858 14.6893 14.25 15.1035 14.25H18.6035C19.0177 14.25 19.3535 14.5858 19.3535 15V18.5C19.3535 18.9142 19.0177 19.25 18.6035 19.25H15.1035C14.6893 19.25 14.3535 18.9142 14.3535 18.5V15Z" fill="#ffffff"/>
                    </svg>

                </button>
            }
        </aside>

        <div className='content'>
            {isOpen && <div className={styles.aside_quit} onClick={() => setIsOpen(!isOpen)} />}

            <header className={styles.header}>
                <h1 className={styles.header__title}>{getPageTitle(path)}</h1>
            
                <div className={styles.header__menu}>            
                    <button className={styles.header__menu__user}>
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className={styles.header__menu__user_icon}>
                            <path fillRule="evenodd" d="M7.5 6a4.5 4.5 0 1 1 9 0 4.5 4.5 0 0 1-9 0ZM3.751 20.105a8.25 8.25 0 0 1 16.498 0 .75.75 0 0 1-.437.695A18.683 18.683 0 0 1 12 22.5c-2.786 0-5.433-.608-7.812-1.7a.75.75 0 0 1-.437-.695Z" clipRule="evenodd" />
                        </svg>
                    </button>
                </div>
            </header>
            
            <main className={styles.main}>
                <Outlet />
            </main>
            <footer className={styles.footer}>
                <span className={styles.footer__title}>~ projet L3K1 ~</span>
            </footer>
        </div>
    </div>
  )
}

export default Layout