"use client"

import styles from "./modules/loading_panel.module.css";
import { faSpinner } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useSelector } from "react-redux";

export default function LoadingPanel({ session_required }){
    const session = useSelector((state) => state.userSession);

    if (session.loading || (session_required && session.error) || (!session_required && session.username)){
        return (
            <div className={styles.overlay}>
                <FontAwesomeIcon icon={faSpinner} pulse className={styles.loading} />
            </div>
        );
    } else {
        return null;
    }
}