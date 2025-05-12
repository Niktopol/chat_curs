"use client"

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import ChatPanels from "./chat_panels";
import styles from "./modules/chats.module.css"
import Profile from "./profile";
import { faCircleUser, faMagnifyingGlass, faXmark} from "@fortawesome/free-solid-svg-icons";
import { useState } from "react";

export default function Chats(){
    const [isProfileHidden, setIsProfileHidden] = useState(null);
    const [searchVal, setSearchVal] = useState("");
    const [timer, setTimer] = useState(null);

    return (
        <div className={styles.main}>
            <div className={styles.search_block}>
                <div className={styles.chats_profile_toggle} onClick={() => setIsProfileHidden(isProfileHidden == null? false : !isProfileHidden)} >
                    <FontAwesomeIcon icon={faCircleUser} className={`${styles.chats_profile_button} ${isProfileHidden != null && !isProfileHidden ? styles.hidden: ""}`} />
                    <FontAwesomeIcon icon={faXmark} className={`${styles.chats_profile_button} ${isProfileHidden == null || isProfileHidden ? styles.hidden: ""}`} />
                </div>
                <div className={styles.searchbar}>
                    <FontAwesomeIcon icon={faMagnifyingGlass} className={styles.search_icon}/>
                    <input
                        placeholder="Поиск"
                        maxLength={21}
                        disabled={isProfileHidden != null && !isProfileHidden}
                        onChange={(e) => {
                            clearTimeout(timer);
                            const newTimer = setTimeout(() => {
                                if (/^[A-Za-z0-9\-=_#+&$@]+$/.test(e.target.value)){
                                    setSearchVal(e.target.value);
                                } else {
                                    setSearchVal("");
                                }
                            }, 250);
                            setTimer(newTimer);
                        }}
                        className={styles.searchbar_input}/>
                </div>
            </div>
            <div className={styles.panel_wrapper}>
                <ChatPanels searchVal={searchVal}></ChatPanels>
                <Profile hidden={isProfileHidden}></Profile>
            </div>
        </div>
    );
}