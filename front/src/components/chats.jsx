"use client"

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import ChatPanels from "./chat_panels";
import styles from "./modules/chats.module.css"
import Profile from "./profile";
import { faCircleUser, faMagnifyingGlass, faMessage, faSpinner } from "@fortawesome/free-solid-svg-icons";
import { useState } from "react";

export default function Chats(){
    const [isHidden, setIsHidden] = useState(null);

    return (
        <div className={styles.main}>
            <div className={styles.search_block}>
                <div className={styles.chats_profile_toggle} onClick={() => setIsHidden(isHidden == null? false : !isHidden)} >
                    <FontAwesomeIcon icon={faCircleUser} className={`${styles.chats_profile_button} ${isHidden != null && !isHidden ? styles.hidden: ""}`} />
                    <FontAwesomeIcon icon={faMessage} className={`${styles.chats_profile_button} ${isHidden == null || isHidden ? styles.hidden: ""}`} />
                </div>
                <div className={styles.searchbar}>
                    <FontAwesomeIcon icon={faMagnifyingGlass} className={styles.search_icon}/>
                    <input className={styles.searchbar_input} placeholder="Поиск"></input>
                </div>
            </div>
            <div className={styles.panel_wrapper}>
                <ChatPanels></ChatPanels>
                <Profile hidden={isHidden}></Profile>
            </div>
        </div>
    );
}