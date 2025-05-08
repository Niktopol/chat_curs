"use client"

import Image from "next/image";
import styles from "./modules/profile.module.css"
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFileImport, faPen } from "@fortawesome/free-solid-svg-icons";

export default function Profile({ hidden }){
    return (
        <div className={`${styles.main} ${hidden != null ? (hidden ? styles.slide_left : styles.slide_right) : ""}`}>
            <p className={styles.id}><span>@</span>123123</p>
            <form className={styles.edit_form}>
                <label htmlFor="profileimage" className={`${styles.image} ${styles.disabled}`}>
                    <Image src="/nexts.svg" alt="" fill draggable={false} style={{objectFit: "cover", zIndex: 2}}></Image>
                    <div className={styles.alt_text}>N</div>
                    <div className={styles.image_edit_cover}>
                        <FontAwesomeIcon icon={faFileImport} className={styles.image_edit_cover_icon} />
                    </div>
                </label>
                <input type="file" accept="image/jpeg" id="profileimage" style={{display: "none"}} disabled></input>
                <div className={styles.nickname}>
                    <input className={styles.nickname_input} maxLength={20} placeholder="Введите никнейм" disabled></input>
                </div>
                <button type="button" className={styles.edit_button}>Изменить</button>
            </form>
        </div>
    );
}