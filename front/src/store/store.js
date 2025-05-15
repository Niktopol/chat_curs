import { configureStore } from '@reduxjs/toolkit';
import userSessionReducer from './userSessionSlice';
import chatSelectedReducer from './chatSelectedSlice';
import websocketMessageSlice from './websocketMessageSlice';

export const store = configureStore({
  reducer: {
    userSession: userSessionReducer,
    chatSelected: chatSelectedReducer,
    websocketMessage: websocketMessageSlice
  },
});