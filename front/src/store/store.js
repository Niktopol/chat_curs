import { configureStore } from '@reduxjs/toolkit';
import userSessionReducer from './userSessionSlice';
import chatSelectedReducer from './chatSelectedSlice';

export const store = configureStore({
  reducer: {
    userSession: userSessionReducer,
    chatSelected: chatSelectedReducer,
  },
});