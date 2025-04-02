import React from 'react'
import clsx from 'clsx';

import styles from "./Timeline.module.css"



const Timeline = ({date, progress, last}) => {
    return (
    <div className={styles.element}>
        <div className={clsx(styles.element__progress, last ? styles.element__last:"")}
            style={{"--progress-height": `calc(${progress}*1.5px)`}}
        />
        <span className={styles.element__date}>{date}</span>
    </div>
  )
}

export default Timeline