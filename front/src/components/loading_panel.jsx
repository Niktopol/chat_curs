"use client"

import styles from "./modules/loading_panel.module.css";
import { faSpinner } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useSelector } from "react-redux";

export default function LoadingPanel({ ignore_error }){
    const session = useSelector((state) => state.userSession);
    // useEffect(() => {
    //     setIsEditing(false);
    // }, [session]);

    if (session.loading || (session.error && !ignore_error)){
        return (
            <div className={styles.overlay}>
                <FontAwesomeIcon icon={faSpinner} pulse className={styles.loading} />
            </div>
        );
    } else {
        return null;
    }
}