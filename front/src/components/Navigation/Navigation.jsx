import React from 'react'
import clsx from 'clsx'
import { Link } from 'react-router-dom'

/* style */
import styles from './Navigation.module.css';


const Navigation = ({icon, title, links, activePath}) => {
    return (
    <div className={styles.navigation}>
        <span className={styles.navigation__title}>
            {icon} {title}
        </span>
        
        <ul>
            {links.map(({label, path, page}, i) => (
                <li key={i} className={styles.navigation__tab}>
                    {
                        typeof page !== 'undefined' && activePath !== path ? 
                        <Link className={styles.navigation__link} to={path}>{label}</Link>
                        :
                        <span className={clsx(styles.navigation__link, styles.locked, activePath == path ? styles.active:"")}>{label}</span>
                    }
                </li>
            ))}
        </ul>
    </div>
  )
}

export default Navigation