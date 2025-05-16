"use client"

import Image from "next/image";
import styles from "./modules/chat_user_panel.module.css";
import { useEffect, useState } from "react"
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faEllipsis } from "@fortawesome/free-solid-svg-icons";
import { useRouter } from "next/navigation";
import { useSelector } from "react-redux";

export default function ChatUserPanel({ chatId, data, isOwner }){
    const websocket = useSelector((state) => state.websocketMessage);
    const [image, setImage] = useState("/default_user.svg");
    const [contextOpen, setContextOpen] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [currentName, setCurrentName] = useState(name);
    const router = useRouter();

    useEffect(() => {
        return () => {
            if (image.startsWith('blob:')) {
                URL.revokeObjectURL(image);
            }
        }
    }, [image]);

    useEffect(() => {
        setCurrentName(data.name);
    }, [data.name]);

    useEffect(() => {
        const fetchImage = async () => {
            const userpic_resp = await fetch(`http://localhost:8080/user/profilepic/${encodeURIComponent(data.username)}`, {credentials: "include"});

            if (userpic_resp.ok) {
                const blob = await userpic_resp.blob();
                setImage(URL.createObjectURL(blob));
            }
        }

        const fetchProfile = async () => {
            try {
                const resp = await fetch(`http://localhost:8080/user/profile/${encodeURIComponent(data.username)}`, {credentials: "include"});
                
                if (!resp.ok) {
                    throw new Error((await resp.json())?.error);
                } else {
                    setCurrentName((await resp.json()).name);
                }
            } catch (e) {
                router.push("/login")
            }
        };

        if (websocket.message?.title === "User info updated" && websocket.message.username == data.username) {
            fetchProfile();
            fetchImage();
        }
    },[websocket]);

    useEffect(() => {
        const fetchImage = async () => {
            const userpic_resp = await fetch(`http://localhost:8080/user/profilepic/${encodeURIComponent(data.username)}`, {credentials: "include"});

            if (userpic_resp.ok) {
                const blob = await userpic_resp.blob();
                setImage(URL.createObjectURL(blob));
            }
        }

        fetchImage();
    }, [data.username])

    const delUser = async () => {
        if (!isSubmitting){
            setContextOpen(false);
            setIsSubmitting(true);

            try {
                const del_user_resp = await fetch(`http://localhost:8080/chats/users/${chatId}/delete`, {
                    method: "PATCH",
                    credentials: "include",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({value: data.username}),
                });

                if (!del_user_resp.ok){
                    router.push("/login");
                }
                setIsSubmitting(false);
            } catch (e) {
                router.push("/login");
            }
        }
    }

    const promoteUser = async () => {
        if (!isSubmitting){
            setContextOpen(false);
            setIsSubmitting(true);
            
            try {
                const promote_user_resp = await fetch(`http://localhost:8080/chats/users/${chatId}/owner`, {
                    method: "PATCH",
                    credentials: "include",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({value: data.username}),
                });

                if (!promote_user_resp.ok){
                    router.push("/login");
                }

                setIsSubmitting(false);
            } catch (e) {
                router.push("/login");
            }
        }
    }

    return (
        <div className={styles.main}>
            <div className={styles.details}>
                <div className={styles.image}>
                    <div className={styles.image_wrapper}>
                        <Image src={image} alt="" fill draggable={false} style={{objectFit: "cover"}}></Image>
                    </div>
                </div>
                <div className={styles.user_info}>
                    <p className={styles.info}><span>@</span>{data.username}</p>
                    <p className={styles.name}>{currentName}{data?.isOwner ? <span className={styles.role}>Владалец</span> : <></>}</p>
                    <p className={styles.info}>{data.isOnline ? "В сети" : "Не в сети"}</p>
                </div>
                {isOwner && !data.isOwner ?
                    <div className={styles.context_wrapper}>
                        <div onClick={() => setContextOpen(!contextOpen)} className={styles.actions}>
                            <FontAwesomeIcon icon={faEllipsis} className={styles.actions_icon}/>
                        </div>
                        <div style={contextOpen ? null : {display: "none"}} className={styles.context}>
                            <p onClick={delUser} className={styles.context_option}>Удалить из чата</p>
                            <p onClick={promoteUser} className={styles.context_option}>Сделать владельцем</p>
                        </div>
                    </div>:
                    <></>
                }
                
            </div>
        </div>
    );
}