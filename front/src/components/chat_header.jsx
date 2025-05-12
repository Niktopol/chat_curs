import Image from "next/image";
import styles from "./modules/chat_header.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faXmark } from "@fortawesome/free-solid-svg-icons";
import { useState } from "react";

export default function ChatHeader({ name }){
    const [editOpen, setEditOpen] = useState(false);

    return (
        <>
            <div onClick={() => setEditOpen(true)} className={styles.main}>
                <div className={styles.content}>
                    <div className={styles.image}>
                        <div className={styles.image_wrapper}>
                            <Image src={"/globe.svg"} alt="" fill draggable={false} style={{objectFit: "cover"}}></Image>
                        </div>
                    </div>
                    <div className={styles.info}>
                        <p className={styles.name}>{name}</p>
                        <p className={styles.status}>dasdasd</p>
                    </div>
                </div>
                <div className={styles.background}></div>
            </div>
            { editOpen ? 
                <div className={styles.chat_edit}>
                    <FontAwesomeIcon icon={faXmark} onClick={() => setEditOpen(false)} className={styles.close} />
                </div> :
                <></>
            }
            
        </>
    );
}