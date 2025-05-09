"use client"

import Image from "next/image";
import styles from "./modules/profile.module.css"
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFileImport, faPen } from "@fortawesome/free-solid-svg-icons";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";

export default function Profile({ hidden }){
    const [isEditing, setIsEditing] = useState(null);
    const [submittedData, setSubmittedData] = useState(null);
    const [userImage, setUserImage] = useState(null);

    useEffect(() => {
        setIsEditing(false);
    }, [hidden]);

    useEffect(() => {
        return () => {
            if (userImage) URL.revokeObjectURL(userImage);
        }
    }, [userImage]);

    useEffect(() => {
        if (submittedData){
            reset({nickname: submittedData.nickname});
        }
    }, [submittedData])

    const resetEditing = () => {
        setIsEditing(false); 
        setUserImage(null);
        reset();
    }

    const {
        register,
        handleSubmit,
        reset,
        setError,
        formState: { errors },
    } = useForm({defaultValues: {nickname: "kirill"}});

    const onSubmit = (data) => {
        setIsEditing(false);
        setSubmittedData({nickname: data.nickname})
    };

    return (
        <div className={`${styles.main} ${hidden != null ? (hidden ? styles.slide_left : styles.slide_right) : ""}`}>
            <p className={styles.id}><span>@</span>123123</p>
            <form className={styles.edit_form} onSubmit={handleSubmit(onSubmit)}>
                <label htmlFor="profileimage" className={`${styles.image} ${!isEditing ? styles.disabled: ""}`}>
                    <Image src={userImage ? userImage : "/none.svg"} alt="" fill draggable={false} style={{objectFit: "cover", zIndex: 2}}></Image>
                    <div className={styles.alt_text}>N</div>
                    <div className={styles.image_edit_cover}>
                        <FontAwesomeIcon icon={faFileImport} className={styles.image_edit_cover_icon} />
                    </div>
                </label>
                <input type="file"
                {...register("file", {
                    onChange: (e) => {
                        if (e.target.files[0].size < 10 * 1024 * 1024){
                            setUserImage(URL.createObjectURL(e.target.files[0]));
                        }
                    },
                    validate: {
                        lessThan10MB: (fileList) =>
                        !fileList.length || fileList[0]?.size < 10 * 1024 * 1024 || "Файл должен быть меньше 10MB",
                    },
                })}
                accept="image/jpeg" id="profileimage" style={{display: "none"}} disabled={!isEditing}  />
                <div className={styles.nickname}>
                    <input className={styles.nickname_input}
                    {...register("nickname", { required: "Введите никнейм" })}
                    maxLength={20} placeholder="Введите никнейм" disabled={!isEditing} />
                </div>
                {!isEditing && (<button type="button" onClick={() => setIsEditing(true)} className={styles.edit_button}>Изменить</button>)}
                {isEditing && (<><button type="submit" className={styles.edit_button}>Сохранить</button>
                               <button type="button" className={styles.edit_button} onClick={resetEditing}>Отменить</button></>)}
            </form>
        </div>
    );
}