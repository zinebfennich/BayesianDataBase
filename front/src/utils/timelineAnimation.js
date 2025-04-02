
import styles from "../components/Timeline/Timeline.module.css"
import homeStyles from "../pages/Home/Home.module.css"


const timelineAnimation = () => {
    const timeline = document.querySelector(`.${homeStyles.timeline__baseline}`);
    const timelineProgressBar = document.querySelectorAll(`.${styles.element__progress}`);

    const observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
            if(entry.isIntersecting){
                timelineProgressBar.forEach(progressBar => {
                    progressBar.classList.add(styles.expend);
                })
            }
            else{
                timelineProgressBar.forEach(progressBar => {
                    progressBar.classList.remove(styles.expend);
                })            
            }
        })
    })

    observer.observe(timeline)   
}

export default timelineAnimation;