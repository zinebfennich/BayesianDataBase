import React from 'react'
import { Link } from 'react-router-dom'
import clsx from 'clsx'

import { dataScience } from '../../../constants/index.jsx'

/* style */
import styles from "./Datascience.module.css"


const Datascience = () => {
  return (
    <>
        <h2 className='title'>Analyse</h2>
        <div className={styles.item__container}>
            {
                dataScience.map(({id, title, icon, description, link}) => (
                    typeof link.page !== 'undefined'?
                    <Link to={link.path} key={id} className={styles.item}>
                        <div>
                            <h3 className={styles.item__title}>{title}</h3>
                            <div className={styles.item__icon}>{icon}</div>
                        </div>

                        <p className={styles.item__description}>{description}</p>
                    </Link>
                    :
                    <div key={id} className={clsx(styles.item, styles.locked)}>
                        <div>
                            <h3 className={styles.item__title}>{title}</h3>
                            <div className={styles.item__icon}>{icon}</div>
                        </div>

                        <p className={styles.item__description}>{description}</p>
                    </div>
                ))
            }
        </div>
    </>
  )
}

export default Datascience